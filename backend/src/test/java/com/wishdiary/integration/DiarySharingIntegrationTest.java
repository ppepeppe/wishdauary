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
    private int fixtureDay;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ChildSignupUseCase signup;
    @Autowired FamilySignupUseCase familySignup;
    @Autowired ManageSharingGroupsUseCase groups;
    @Autowired org.springframework.jdbc.core.JdbcTemplate database;

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
            assertFalse(user.has("loginId"));
            assertFalse(user.has("accessToken"));
            assertFalse(user.has("groupId"));
        }
        assertTrue(found);
        assertEquals(0, call(get("/api/v1/sharing-groups/users").param("query", " "), owner, null, 200).size());
    }

    @Test
    @Transactional
    void defaultFamilyGroupCountsTowardFiveGroupLimit() throws Exception {
        var owner = child("family-limit");
        database.update("insert into diary_sharing_groups(owner_id,name,slot) values (?, ?, 0)", owner.userId(), "우리 가족");
        for (int i = 0; i < 4; i++) group(owner, "group" + i, List.of());
        assertEquals(5, groups.list(owner.userId()).size());
        call(post("/api/v1/sharing-groups"), owner, Map.of("name", "sixth", "memberIds", List.of()), 400);
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

    @Test
    @Transactional
    void groupTabsOnlyExposeMembershipAndFilterBeforePagination() throws Exception {
        var owner = child("탭 작성자"); var viewer = child("탭 독자"); var outsider = child("외부 아이");
        long first = group(owner, "친구", List.of(viewer.userId()));
        long second = group(owner, "비공개 모임", List.of());
        long shared = write(owner, "group", List.of(first, second));
        long hidden = write(owner, "group", List.of(second));
        long published = write(owner, "public", List.of());
        JsonNode visible = call(get("/api/v1/diaries/shared-feed/groups"), viewer, null, 200);
        assertTrue(visible.toString().contains("친구"));
        assertFalse(visible.toString().contains("비공개 모임"));
        JsonNode chosen = call(get("/api/v1/diaries/shared-feed").param("groupId", String.valueOf(first)), viewer, null, 200);
        assertEquals(1, chosen.size()); assertEquals(shared, chosen.get(0).get("diaryId").asLong());
        assertEquals(0, call(get("/api/v1/diaries/shared-feed").param("groupId", String.valueOf(second)), viewer, null, 200).size());
        assertEquals(0, call(get("/api/v1/diaries/shared-feed").param("groupId", String.valueOf(first)), outsider, null, 200).size());
        JsonNode onlyPublic = call(get("/api/v1/diaries/shared-feed").param("groupId", "0"), viewer, null, 200);
        assertTrue(java.util.stream.StreamSupport.stream(onlyPublic.spliterator(), false).anyMatch(item -> item.get("diaryId").asLong() == published));
        for (var item : onlyPublic) assertEquals("public", item.get("visibilityScope").asText());
        for (var item : call(get("/api/v1/diaries/shared-feed").param("groupId", "-1"), viewer, null, 200)) {
            assertEquals("group", item.get("visibilityScope").asText()); assertNotEquals(hidden, item.get("diaryId").asLong());
        }
        call(get("/api/v1/diaries/shared-feed/groups"), null, null, 401);
    }

    @Test
    @Transactional
    void groupMembersUseEachUsersCurrentProfileWithoutCopyingItIntoTheGroup() throws Exception {
        var owner = child("프로필 주인"); var member = child("프로필 구성원"); var adult = family(owner);
        call(patch("/api/v1/users/me"), member, Map.of("nickname", "프로필 구성원", "avatarPreset", "mint", "avatarCharacter", "kuromi"), 200);
        String image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+a47sAAAAASUVORK5CYII=";
        call(patch("/api/v1/users/me"), adult, Map.of("nickname", "프로필 가족", "avatarPreset", "sky", "avatarCharacter", "cinnamoroll", "avatarImage", image), 200);
        JsonNode created = call(post("/api/v1/sharing-groups"), owner,
                Map.of("name", "프로필 그룹", "memberIds", List.of(member.userId(), adult.userId())), 201);
        assertEquals(2, created.get("members").size());
        for (JsonNode person : created.get("members")) {
            if (person.get("userId").asLong() == member.userId()) {
                assertEquals("kuromi", person.path("avatarCharacter").asText());
                assertEquals("mint", person.path("avatarPreset").asText());
                assertTrue(person.path("avatarImage").isNull());
            } else {
                assertEquals("cinnamoroll", person.path("avatarCharacter").asText());
                assertEquals(image, person.path("avatarImage").asText());
            }
            assertFalse(person.has("loginId")); assertFalse(person.has("passwordHash"));
        }
        call(patch("/api/v1/users/me"), adult, Map.of("nickname", "새 프로필 가족", "avatarPreset", "rose", "avatarCharacter", "my-melody"), 200);
        for (JsonNode group : call(get("/api/v1/sharing-groups"), owner, null, 200)) {
            for (JsonNode person : group.get("members")) if (person.get("userId").asLong() == adult.userId()) {
                assertEquals("새 프로필 가족", person.get("nickname").asText());
                assertEquals("my-melody", person.path("avatarCharacter").asText());
                assertEquals("rose", person.path("avatarPreset").asText());
                assertTrue(person.path("avatarImage").isNull());
            }
        }
    }

    @Test
    @Transactional
    void combinedGroupFeedIncludesBothChildrenOnceAndKeepsGroupPermissions() throws Exception {
        var first = child("첫 아이"); var second = child("둘째 아이"); var viewer = family(first); var outsider = child("외부 아이");
        long a = group(first, "우리 가족", List.of(viewer.userId()));
        long overlap = group(first, "겹치는 모임", List.of(viewer.userId()));
        long b = group(second, "우리 가족", List.of(viewer.userId()));
        long secret = group(second, "비공개 모임", List.of());
        long firstDiary = write(first, "group", List.of(a, overlap));
        long secondDiary = write(second, "group", List.of(b));
        write(second, "group", List.of(secret)); write(second, "public", List.of());
        String selected = a + "," + overlap + "," + b + "," + secret;
        JsonNode items = call(get("/api/v1/diaries/shared-feed").param("groupIds", selected), viewer, null, 200);
        assertEquals(Set.of(firstDiary, secondDiary), feedIds(items)); assertEquals(2, items.size());
        assertEquals(0, call(get("/api/v1/diaries/shared-feed").param("groupIds", "" + secret), viewer, null, 200).size());
        assertEquals(0, call(get("/api/v1/diaries/shared-feed").param("groupIds", selected), outsider, null, 200).size());
        call(get("/api/v1/diaries/shared-feed").param("groupIds", "0,-1"), viewer, null, 400);
        call(get("/api/v1/diaries/shared-feed").param("groupIds", selected).param("groupId", "0"), viewer, null, 400);
        call(get("/api/v1/diaries/shared-feed").param("groupIds", selected), null, null, 401);
        String prefix = "pagination-" + UUID.randomUUID();
        for (int index = 0; index < 31; index++) database.update(
                "insert into diaries(author_id,content,visibility_scope,written_date,created_at) values(?,?,'GROUP',?,?)",
                first.userId(), prefix + index, LocalDate.now().minusDays(100 + index), java.time.LocalDateTime.now().plusDays(1).plusSeconds(index));
        database.update("insert into diary_audience_groups(diary_id,sharing_group_id) select id,? from diaries where content like ?", a, prefix + "%");
        JsonNode pageOne = call(get("/api/v1/diaries/shared-feed").param("groupIds", selected), viewer, null, 200);
        JsonNode pageTwo = call(get("/api/v1/diaries/shared-feed").param("groupIds", selected).param("page", "1"), viewer, null, 200);
        assertEquals(30, pageOne.size()); assertEquals(3, pageTwo.size());
        Set<Long> all = feedIds(pageOne); all.addAll(feedIds(pageTwo)); assertEquals(33, all.size());
        assertTrue(all.contains(firstDiary)); assertTrue(all.contains(secondDiary));
    }

    @Test
    @Transactional
    void connectedChildCardsReadTheChildsCurrentProfile() throws Exception {
        var owner = child("프로필 아이"); var adult = family(owner);
        call(patch("/api/v1/users/me"), owner, Map.of("nickname", "프로필 아이", "avatarPreset", "mint", "avatarCharacter", "kuromi"), 200);
        JsonNode people = call(get("/api/v1/family-connections"), adult, null, 200);
        assertEquals(1, people.size()); assertEquals(owner.userId(), people.get(0).get("userId").asLong());
        assertEquals("kuromi", people.get(0).path("avatarCharacter").asText());
        assertEquals("mint", people.get(0).path("avatarPreset").asText());
        assertTrue(people.get(0).path("avatarImage").isNull());
    }

    private Set<Long> feedIds(JsonNode items) {
        Set<Long> ids = new HashSet<>(); items.forEach(item -> ids.add(item.get("diaryId").asLong())); return ids;
    }

    private SignupResponse child(String nickname) {
        return signup.signup(new ChildSignupRequest(UUID.randomUUID() + "@test.com", "password", TestNicknames.unique(nickname)));
    }

    private SignupResponse family(SignupResponse child) {
        SignupResponse family = familySignup.signup(new FamilySignupRequest(UUID.randomUUID() + "@test.com", "password", TestNicknames.unique("family"),
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
        return Map.of("content", "마음 기록", "visibilityScope", scope, "sharingGroupIds", groupIds, "writtenDate", LocalDate.now().minusDays(2L * fixtureDay++).toString());
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
        return json.readTree(response.getContentAsByteArray()).get("data");
    }
}
