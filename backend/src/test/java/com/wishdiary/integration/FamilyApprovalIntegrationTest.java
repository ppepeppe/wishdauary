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
class FamilyApprovalIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    private static final String BASE = "/api/v1/family-connections";

    @Test
    void searchingRequiresFamilyLoginAndReturnsOnlyChildIdentity() throws Exception {
        JsonNode child = signup("child", "승인테스트아이");
        JsonNode family = signup("family", "승인테스트가족");
        call(get(BASE + "/children").param("query", "승인테스트"), null, null, 401);
        call(get(BASE + "/children").param("query", "승인테스트"), child, null, 403);
        JsonNode found = call(get(BASE + "/children").param("query", "승인테스트"), family, null, 200);
        assertEquals(1, found.size());
        assertEquals(id(child), found.get(0).get("userId").asLong());
        assertFalse(found.get(0).has("email"));
        call(get(BASE + "/children").param("query", "아"), family, null, 400);
    }

    @Test
    void pendingRequestsGrantNoFamilyAccess() throws Exception {
        JsonNode child = signup("child", "아이");
        JsonNode family = signup("family", "엄마");
        long diary = write(child, "family");
        JsonNode first = request(family, child);
        assertEquals("pending", first.get("status").asText());
        assertEquals(first.get("requestId"), request(family, child).get("requestId"));
        assertEquals(0, call(get("/api/v1/shop/recipients"), child, null, 200).size());
        assertEquals(0, call(get("/api/v1/diaries/family-feed"), family, null, 200).size());
        call(get("/api/v1/diaries/" + diary), family, null, 403);
        call(post("/api/v1/shop/family-products"), family, product(child), 403);
        call(post(BASE + "/requests"), child, Map.of("childUserId", id(child)), 403);
        call(post(BASE + "/requests"), family, Map.of("childUserId", id(family)), 400);
    }

    @Test
    void onlyRequestedChildCanApproveAndAdultsCannotWriteDiaries() throws Exception {
        JsonNode child = signup("child", "아이");
        JsonNode other = signup("child", "다른 아이");
        JsonNode family = signup("family", "엄마");
        long diary = write(child, "family");
        long privateDiary = write(child, "private");
        long requestId = request(family, child).get("requestId").asLong();
        process(other, requestId, "APPROVE", 403);
        process(family, requestId, "APPROVE", 403);
        assertEquals("approved", process(child, requestId, "APPROVE", 200).get("status").asText());
        process(child, requestId, "REJECT", 409);
        assertEquals(1, call(get("/api/v1/shop/recipients"), child, null, 200).size());
        call(get("/api/v1/diaries/" + diary), family, null, 200);
        call(get("/api/v1/diaries/" + privateDiary), family, null, 403);
        call(post("/api/v1/reactions/comments"), family, Map.of("diaryId", diary, "content", "응원해요"), 200);
        assertEquals(1, call(get("/api/v1/reactions/feed"), child, null, 200).size());
        call(post("/api/v1/reactions/comments"), family, Map.of("diaryId", privateDiary, "content", "볼 수 없어요"), 403);
        call(post("/api/v1/diaries"), family, Map.of("content", "어른 일기", "visibilityScope", "public", "writtenDate", LocalDate.now().toString()), 403);
        JsonNode uploaded = call(post("/api/v1/shop/family-products"), family, product(child), 201);
        assertEquals("family", uploaded.get("source").asText());
        assertTrue(call(get("/api/v1/shop/products"), child, null, 200).findValues("title").stream().anyMatch(value -> value.asText().equals("가족 선물")));
        assertFalse(call(get("/api/v1/shop/products"), other, null, 200).findValues("title").stream().anyMatch(value -> value.asText().equals("가족 선물")));
        call(post("/api/v1/shop/family-products"), family, product(other), 403);
        call(post("/api/v1/shop/family-products"), child, product(child), 403);
    }

    @Test
    void rejectionCanBeRetriedAndApprovalDoesNotConsumeCustomGroupSlots() throws Exception {
        JsonNode child = signup("child", "아이");
        JsonNode family = signup("family", "엄마");
        for (int i=0; i<5; i++) call(post("/api/v1/sharing-groups"), child, Map.of("name", "친구" + i, "memberIds", List.of()), 201);
        long requestId = request(family, child).get("requestId").asLong();
        process(child, requestId, "REJECT", 200);
        assertEquals(0, call(get(BASE), family, null, 200).size());
        assertEquals("pending", request(family, child).get("status").asText());
        process(child, requestId, "APPROVE", 200);
        JsonNode groups = call(get("/api/v1/sharing-groups"), child, null, 200);
        assertEquals(6, groups.size());
        JsonNode familyGroup = groups.findValues("defaultGroup").stream().anyMatch(JsonNode::asBoolean) ? groups.get(0) : null;
        assertNotNull(familyGroup);
        call(delete("/api/v1/sharing-groups/" + familyGroup.get("id").asLong()), child, null, 200);
        assertEquals(0, call(get(BASE), family, null, 200).size());
        call(post("/api/v1/shop/family-products"), family, product(child), 403);
        assertEquals("pending", request(family, child).get("status").asText());
    }

    @Test
    void childAddingFamilyToGroupAlsoApprovesTheirPendingRequest() throws Exception {
        JsonNode child = signup("child", "아이");
        JsonNode family = signup("family", "엄마");
        request(family, child);
        call(post("/api/v1/sharing-groups"), child, Map.of("name", "가족", "memberIds", List.of(id(family))), 201);
        assertEquals("approved", call(get(BASE + "/requests"), family, null, 200).get(0).get("status").asText());
        assertEquals(1, call(get(BASE), family, null, 200).size());
    }

    private Map<String,Object> product(JsonNode child) {
        return Map.of("childUserId", id(child), "title", "가족 선물", "description", "함께 골랐어요", "cloverPrice", 5);
    }
    private JsonNode request(JsonNode family, JsonNode child) throws Exception {
        return call(post(BASE + "/requests"), family, Map.of("childUserId", id(child)), 201);
    }
    private JsonNode process(JsonNode user, long id, String action, int status) throws Exception {
        return call(patch(BASE + "/requests/" + id), user, Map.of("action", action), status);
    }
    private long write(JsonNode child, String scope) throws Exception {
        return call(post("/api/v1/diaries"), child, Map.of("content", "오늘 기록", "visibilityScope", scope, "writtenDate", LocalDate.now().toString()), 201).get("diaryId").asLong();
    }
    private JsonNode signup(String role, String nickname) throws Exception {
        var body = new java.util.HashMap<String,Object>(Map.of("email", UUID.randomUUID()+"@test.com", "password", "password", "nickname", nickname));
        if (role.equals("family")) { body.put("familyRelation", "MOTHER"); body.put("relationLabel", "엄마"); }
        return call(post("/api/v1/auth/signup/"+role), null, body, 201);
    }
    private long id(JsonNode user) { return user.get("userId").asLong(); }
    private JsonNode call(MockHttpServletRequestBuilder request, JsonNode user, Object body, int expected) throws Exception {
        if (user != null) request.header("Authorization", "Bearer "+user.get("accessToken").asText());
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        var response=mvc.perform(request).andReturn().getResponse();
        assertEquals(expected, response.getStatus(), response.getContentAsString());
        return json.readTree(response.getContentAsByteArray()).get("data");
    }
}
