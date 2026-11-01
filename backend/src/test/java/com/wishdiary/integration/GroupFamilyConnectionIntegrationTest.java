package com.wishdiary.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
@Transactional
class GroupFamilyConnectionIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void familySignupNeedsNoCode() throws Exception {
        JsonNode family = signup("family", "엄마");
        assertTrue(family.get("inviteCode").isNull());
        assertNotNull(family.get("accessToken"));
        assertEquals(0, call(get("/api/v1/diaries/family-feed"), family, null, 200).size());
    }

    @Test
    void groupsDetermineGiftRecipients() throws Exception {
        JsonNode child = signup("child", "아이");
        JsonNode family = signup("family", "엄마");
        JsonNode friend = signup("child", "친구");
        assertEquals(0, recipients(child).size());
        long first = group(child, "우리 가족", List.of(id(family), id(friend)));
        long second = group(child, "응원단", List.of(id(family)));
        assertEquals(1, recipients(child).size());
        assertEquals(id(family), recipients(child).get(0).get("userId").asLong());
        updateGroup(child, first, List.of());
        assertEquals(1, recipients(child).size());
        call(delete("/api/v1/sharing-groups/" + second), child, null, 200);
        assertEquals(0, recipients(child).size());
    }

    @Test
    void groupMembershipEnablesGiftRequests() throws Exception {
        LinkedFamily link = linkedFamily();
        assertNotEquals(link.child().get("groupId"), link.family().get("groupId"));
        write(link.child(), "family", List.of());
        JsonNode sent = call(post("/api/v1/shop/gift-requests"), link.child(), Map.of(
                "productId", link.productId(), "receiverFamilyUserId", id(link.family()), "message", "부탁해요"), 201);
        assertEquals(0, call(get("/api/v1/clovers/balance"), link.child(), null, 200).get("balance").asInt());
        JsonNode outsider = signup("family", "다른 가족");
        call(patch("/api/v1/shop/gift-requests/" + sent.get("notificationId").asLong()), outsider, Map.of("action", "APPROVE"), 403);
        call(patch("/api/v1/shop/gift-requests/" + sent.get("notificationId").asLong()), link.family(), Map.of("action", "APPROVE"), 200);
        assertEquals("approved", call(get("/api/v1/shop/gift-requests/sent"), link.child(), null, 200).get(0).get("status").asText());
    }

    @Test
    void removingLastConnectionRevokesFamilyAccess() throws Exception {
        LinkedFamily link = linkedFamily();
        long diary = write(link.child(), "family", List.of());
        call(put("/api/v1/wish-goals/me"), link.child(), Map.of("productId", link.productId()), 200);
        assertEquals(1, call(get("/api/v1/wish-goals/children"), link.family(), null, 200).size());
        call(get("/api/v1/diaries/" + diary), link.family(), null, 200);
        updateGroup(link.child(), link.groupId(), List.of());
        assertEquals(0, recipients(link.child()).size());
        assertEquals(0, call(get("/api/v1/wish-goals/children"), link.family(), null, 200).size());
        assertEquals(0, call(get("/api/v1/diaries/family-feed"), link.family(), null, 200).size());
        call(get("/api/v1/diaries/" + diary), link.family(), null, 403);
        call(patch("/api/v1/shop/products/" + link.productId() + "/price"), link.family(), Map.of("cloverPrice", 2), 403);
        call(post("/api/v1/shop/gift-requests"), link.child(), Map.of(
                "productId", link.productId(), "receiverFamilyUserId", id(link.family()), "message", "부탁해요"), 403);
    }

    @Test
    void groupDiaryAudienceStaysLimitedToSelectedGroups() throws Exception {
        JsonNode child = signup("child", "아이");
        JsonNode first = signup("family", "첫 가족");
        JsonNode second = signup("family", "둘째 가족");
        long selected = group(child, "선택한 가족", List.of(id(first)));
        group(child, "다른 가족", List.of(id(second)));
        long diary = write(child, "group", List.of(selected));
        call(get("/api/v1/diaries/" + diary), first, null, 200);
        call(get("/api/v1/diaries/" + diary), second, null, 403);
        long privateDiary = write(child, "private", List.of());
        call(get("/api/v1/diaries/" + privateDiary), first, null, 403);
        assertEquals(0, call(get("/api/v1/diaries/family-feed"), second, null, 200).size());
    }

    @Test
    void oneFamilyAccountCanConnectToMultipleChildren() throws Exception {
        JsonNode family = signup("family", "엄마");
        JsonNode first = signup("child", "첫 아이");
        JsonNode second = signup("child", "둘째 아이");
        long firstGroup = group(first, "가족", List.of(id(family)));
        group(second, "가족", List.of(id(family)));
        write(first, "family", List.of());
        write(second, "family", List.of());
        assertEquals(2, call(get("/api/v1/diaries/family-feed"), family, null, 200).size());
        updateGroup(first, firstGroup, List.of());
        assertEquals(1, call(get("/api/v1/diaries/family-feed"), family, null, 200).size());
        assertEquals(1, recipients(second).size());
    }

    @Test
    void oldFamilyManagementEndpointsCannotChangeConnections() throws Exception {
        LinkedFamily link = linkedFamily();
        call(get("/api/v1/family/invite-code"), link.child(), null, 410);
        call(patch("/api/v1/family/invite-code/regenerate"), link.child(), null, 410);
        call(delete("/api/v1/family/members/" + link.family().get("groupMemberId").asLong()), link.child(), null, 410);
        assertEquals(1, recipients(link.child()).size());
        call(get("/api/v1/diaries/family-feed"), link.family(), null, 200);
    }

    private record LinkedFamily(JsonNode child, JsonNode family, long groupId, long productId) { }

    private LinkedFamily linkedFamily() throws Exception {
        JsonNode child = signup("child", "아이");
        JsonNode family = signup("family", "엄마");
        long product = call(post("/api/v1/shop/products"), child,
                Map.of("title", "색연필", "description", "그림을 그리고 싶어요"), 201).get("productId").asLong();
        call(patch("/api/v1/shop/products/" + product + "/price"), family, Map.of("cloverPrice", 1), 403);
        long group = group(child, "우리 가족", List.of(id(family)));
        call(patch("/api/v1/shop/products/" + product + "/price"), family, Map.of("cloverPrice", 1), 200);
        return new LinkedFamily(child, family, group, product);
    }

    private long write(JsonNode child, String scope, List<Long> groups) throws Exception {
        return call(post("/api/v1/diaries"), child, Map.of("content", "오늘의 기록", "visibilityScope", scope,
                "sharingGroupIds", groups, "writtenDate", LocalDate.now().toString()), 201).get("diaryId").asLong();
    }

    private JsonNode signup(String role, String nickname) throws Exception {
        Map<String, Object> body = new java.util.HashMap<>(Map.of(
                "email", UUID.randomUUID() + "@test.com", "password", "password", "nickname", nickname));
        if (role.equals("family")) { body.put("familyRelation", "MOTHER"); body.put("relationLabel", "엄마"); }
        return call(post("/api/v1/auth/signup/" + role), null, body, 201);
    }

    private long id(JsonNode user) { return user.get("userId").asLong(); }

    private JsonNode recipients(JsonNode child) throws Exception {
        return call(get("/api/v1/shop/recipients"), child, null, 200);
    }

    private long group(JsonNode child, String name, List<Long> members) throws Exception {
        return call(post("/api/v1/sharing-groups"), child, Map.of("name", name, "memberIds", members), 201).get("id").asLong();
    }

    private void updateGroup(JsonNode child, long groupId, List<Long> members) throws Exception {
        call(patch("/api/v1/sharing-groups/" + groupId), child, Map.of("name", "가족", "memberIds", members), 200);
    }

    private JsonNode call(MockHttpServletRequestBuilder request, JsonNode user, Object body, int expected) throws Exception {
        if (user != null) request.header("Authorization", "Bearer " + user.get("accessToken").asText());
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        var response = mvc.perform(request).andReturn().getResponse();
        assertEquals(expected, response.getStatus(), response.getContentAsString());
        return json.readTree(response.getContentAsString()).get("data");
    }
}
