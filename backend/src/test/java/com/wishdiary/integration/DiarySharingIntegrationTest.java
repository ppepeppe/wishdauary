package com.wishdiary.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wishdiary.application.auth.*;
import com.wishdiary.application.sharing.*;
import com.wishdiary.domain.sharing.InvalidSharingGroupException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DiarySharingIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ChildSignupUseCase signup;
    @Autowired FamilySignupUseCase familySignup;
    @Autowired ManageSharingGroupsUseCase groups;

    @Test
    @Transactional
    void groupMembershipControlsFeedDetailsAndCommentsAcrossHouseholds() throws Exception {
        var owner = child("owner");
        var included = child("included");
        var excluded = child("excluded");
        var first = group(owner, "친구", List.of(included.userId()));
        var second = group(owner, "응원단", List.of());
        long diaryId = write(owner, "group", List.of(first, second));
        assertEquals(List.of(first, second), ids(call(get("/api/v1/diaries/" + diaryId), owner, null, 200).get("sharingGroupIds")));
        assertFeedContains(included, diaryId, true);
        assertFeedContains(excluded, diaryId, false);
        call(get("/api/v1/diaries/" + diaryId), included, null, 200);
        call(get("/api/v1/diaries/" + diaryId), excluded, null, 403);
        call(get("/api/v1/reactions/comments").param("diaryId", "" + diaryId), excluded, null, 403);
        call(get("/api/v1/reactions/comments").param("diaryId", "" + diaryId), included, null, 200);
        call(patch("/api/v1/diaries/" + diaryId), included, diaryBody("public", List.of()), 403);
        call(delete("/api/v1/diaries/" + diaryId), included, null, 403);

        call(patch("/api/v1/sharing-groups/" + second), owner, Map.of("name", "응원단", "memberIds", List.of(excluded.userId())), 200);
        assertFeedContains(excluded, diaryId, true);
        call(patch("/api/v1/sharing-groups/" + first), owner, Map.of("name", "친구", "memberIds", List.of()), 200);
        call(get("/api/v1/diaries/" + diaryId), included, null, 403);
        assertFeedContains(included, diaryId, false);
        call(delete("/api/v1/sharing-groups/" + second), owner, null, 200);
        call(get("/api/v1/diaries/" + diaryId), excluded, null, 403);
        call(get("/api/v1/diaries/" + diaryId), owner, null, 200);
        call(post("/api/v1/diaries"), owner, diaryBody("group", List.of(second)), 400);
    }

    @Test
    @Transactional
    void editingAudienceReplacesOldGroupsAndPrivateModeClearsSelections() throws Exception {
        var owner = child("owner");
        var firstMember = child("first");
        var secondMember = child("second");
        long first = group(owner, "first", List.of(firstMember.userId()));
        long second = group(owner, "second", List.of(secondMember.userId()));
        long diaryId = write(owner, "group", List.of(first));
        call(patch("/api/v1/diaries/" + diaryId), owner, diaryBody("group", List.of(second)), 200);
        call(get("/api/v1/diaries/" + diaryId), firstMember, null, 403);
        call(get("/api/v1/diaries/" + diaryId), secondMember, null, 200);
        call(delete("/api/v1/sharing-groups/" + second), owner, null, 200);
        group(owner, "replacement", List.of(secondMember.userId()));
        call(get("/api/v1/diaries/" + diaryId), secondMember, null, 403);
        JsonNode updated = call(patch("/api/v1/diaries/" + diaryId), owner, diaryBody("private", List.of()), 200);
        assertEquals(0, updated.get("sharingGroupIds").size());
        call(delete("/api/v1/diaries/" + diaryId), owner, null, 200);
        call(get("/api/v1/diaries/" + diaryId), owner, null, 404);
    }

    @Test
    @Transactional
    void familyFeedDoesNotRevealGroupsToUnselectedFamilyMembers() throws Exception {
        var owner = child("owner");
        var member = family(owner);
        var other = family(owner);
        long group = group(owner, "선택한 가족", List.of(member.userId()));
        long diaryId = write(owner, "group", List.of(group));
        JsonNode included = call(get("/api/v1/diaries/family-feed"), member, null, 200);
        JsonNode excluded = call(get("/api/v1/diaries/family-feed"), other, null, 200);
        assertEquals(1, included.size());
        assertEquals(diaryId, included.get(0).get("diaryId").asLong());
        assertEquals(0, excluded.size());
        assertEquals("group", included.get(0).get("visibilityScope").asText());
        long publicId = write(owner, "public", List.of());
        JsonNode familyPublicFeed = call(get("/api/v1/diaries/family-feed"), other, null, 200);
        assertEquals(1, familyPublicFeed.size());
        assertEquals(publicId, familyPublicFeed.get(0).get("diaryId").asLong());
        assertEquals("public", familyPublicFeed.get(0).get("visibilityScope").asText());
    }

    @Test
    @Transactional
    void publicDiariesReachAllUsersButPrivateAndLegacyFamilyDiariesDoNot() throws Exception {
        var owner = child("owner");
        var other = child("other");
        long publicId = write(owner, "public", List.of());
        long privateId = write(owner, "private", List.of());
        long familyId = write(owner, "family", List.of());
        assertFeedContains(other, publicId, true);
        assertFeedContains(other, privateId, false);
        assertFeedContains(other, familyId, false);
        call(get("/api/v1/diaries/" + publicId), other, null, 200);
        call(get("/api/v1/diaries/" + privateId), other, null, 403);
        call(get("/api/v1/diaries/" + familyId), other, null, 403);
        call(get("/api/v1/diaries/shared-feed"), null, null, 401);
        call(get("/api/v1/diaries/" + publicId), null, null, 401);
        call(patch("/api/v1/diaries/" + publicId), owner, diaryBody("private", List.of()), 200);
        assertFeedContains(other, publicId, false);
        call(get("/api/v1/diaries/" + publicId), other, null, 403);
    }

    @Test
    @Transactional
    void groupManagementRequiresOwnershipAndValidSelections() throws Exception {
        var owner = child("owner");
        var other = child("other");
        long id = group(owner, "우리", List.of(other.userId()));
        call(patch("/api/v1/sharing-groups/" + id), other, Map.of("name", "stolen", "memberIds", List.of()), 403);
        call(delete("/api/v1/sharing-groups/" + id), other, null, 403);
        call(post("/api/v1/diaries"), other, diaryBody("group", List.of(id)), 400);
        call(post("/api/v1/diaries"), owner, diaryBody("group", List.of()), 400);
        call(post("/api/v1/diaries"), owner, diaryBody("public", List.of(id)), 400);
        call(post("/api/v1/sharing-groups"), owner, Map.of("name", "bad", "memberIds", List.of(Long.MAX_VALUE)), 400);
        call(post("/api/v1/sharing-groups"), owner, Map.of("name", "self", "memberIds", List.of(owner.userId())), 400);
        for (int i = 0; i < 4; i++) group(owner, "group" + i, List.of());
        call(post("/api/v1/sharing-groups"), owner, Map.of("name", "sixth", "memberIds", List.of()), 400);
        call(delete("/api/v1/sharing-groups/" + id), owner, null, 200);
        long replacement = group(owner, "replacement", List.of());
        assertNotEquals(id, replacement);
        assertEquals(5, call(get("/api/v1/sharing-groups"), owner, null, 200).size());
    }

    @Test
    @Transactional
    void familyReactionsHonorGroupMembershipAndPublicVisibility() throws Exception {
        var owner = child("owner");
        var unrelatedChild = child("unrelated");
        var family = family(unrelatedChild);
        long id = group(owner, "응원단", List.of(family.userId()));
        long diaryId = write(owner, "group", List.of(id));
        call(post("/api/v1/reactions/comments"), family, Map.of("diaryId", diaryId, "content", "응원해"), 200);
        call(post("/api/v1/reactions/hearts/toggle"), family, Map.of("diaryId", diaryId), 200);
        call(post("/api/v1/sharing-groups"), family, Map.of("name", "not-child", "memberIds", List.of()), 403);
        call(delete("/api/v1/sharing-groups/" + id), owner, null, 200);
        call(post("/api/v1/reactions/comments"), family, Map.of("diaryId", diaryId, "content", "blocked"), 403);
        call(post("/api/v1/reactions/hearts/toggle"), family, Map.of("diaryId", diaryId), 403);
        call(get("/api/v1/reactions/comments").param("diaryId", "" + diaryId), family, null, 403);
        long publicId = write(owner, "public", List.of());
        call(post("/api/v1/reactions/comments"), family, Map.of("diaryId", publicId, "content", "공개 응원"), 200);
        call(post("/api/v1/reactions/hearts/toggle"), family, Map.of("diaryId", publicId), 200);
    }

    @Test
    @Transactional
    void userSearchReturnsOnlyPublicIdentityAndExcludesSelf() throws Exception {
        var owner = child("search-person");
        var other = child("search-person");
        JsonNode result = call(get("/api/v1/sharing-groups/users").param("query", "search-person"), owner, null, 200);
        assertTrue(result.size() > 0);
        boolean found = false;
        for (JsonNode user : result) {
            assertNotEquals(owner.userId().longValue(), user.get("userId").asLong());
            if (user.get("userId").asLong() == other.userId()) found = true;
            assertFalse(user.has("email"));
            assertFalse(user.has("accessToken"));
            assertFalse(user.has("groupId"));
        }
        assertTrue(found);
        assertEquals(0, call(get("/api/v1/sharing-groups/users").param("query", " "), owner, null, 200).size());
    }

    @Test
    void concurrentCreationCannotExceedFiveGroups() throws Exception {
        var owner = child("concurrent");
        ExecutorService executor = Executors.newFixedThreadPool(6);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Boolean>> attempts = new ArrayList<>();
            for (int i = 0; i < 6; i++) attempts.add(executor.submit(() -> {
                start.await();
                try { groups.create(owner.userId(), new SharingGroupRequest("group", Set.of())); return true; }
                catch (InvalidSharingGroupException expected) { return false; }
            }));
            start.countDown();
            int successes = 0;
            for (Future<Boolean> attempt : attempts) if (attempt.get(20, TimeUnit.SECONDS)) successes++;
            assertEquals(5, successes);
            assertEquals(5, groups.list(owner.userId()).size());
        } finally { executor.shutdownNow(); }
    }

    private SignupResponse child(String nickname) {
        return signup.signup(new ChildSignupRequest(UUID.randomUUID() + "@test.com", "password", nickname));
    }

    private SignupResponse family(SignupResponse child) {
        SignupResponse family = familySignup.signup(new FamilySignupRequest(UUID.randomUUID() + "@test.com", "password", "family",
                com.wishdiary.domain.group.FamilyRelation.MOTHER, "엄마", null));
        groups.create(child.userId(), new SharingGroupRequest("우리 가족", Set.of(family.userId())));
        return family;
    }

    private long group(SignupResponse owner, String name, List<Long> members) throws Exception {
        return call(post("/api/v1/sharing-groups"), owner, Map.of("name", name, "memberIds", members), 201).get("id").asLong();
    }

    private long write(SignupResponse owner, String scope, List<Long> groupIds) throws Exception {
        return call(post("/api/v1/diaries"), owner, diaryBody(scope, groupIds), 201).get("diaryId").asLong();
    }

    private Map<String, Object> diaryBody(String scope, List<Long> groupIds) {
        return Map.of("content", "마음 기록", "visibilityScope", scope, "sharingGroupIds", groupIds, "writtenDate", LocalDate.now().toString());
    }

    private void assertFeedContains(SignupResponse viewer, long diaryId, boolean expected) throws Exception {
        JsonNode items = call(get("/api/v1/diaries/shared-feed"), viewer, null, 200);
        boolean found = false;
        for (JsonNode item : items) if (item.get("diaryId").asLong() == diaryId) found = true;
        assertEquals(expected, found);
    }

    private List<Long> ids(JsonNode values) {
        List<Long> ids = new ArrayList<>();
        values.forEach(value -> ids.add(value.asLong()));
        return ids;
    }

    private JsonNode call(MockHttpServletRequestBuilder request, SignupResponse user, Object body, int expected) throws Exception {
        if (user != null) request.header("Authorization", "Bearer " + user.accessToken());
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        var response = mvc.perform(request).andReturn().getResponse();
        assertEquals(expected, response.getStatus(), response.getContentAsString());
        return json.readTree(response.getContentAsString()).get("data");
    }
}
