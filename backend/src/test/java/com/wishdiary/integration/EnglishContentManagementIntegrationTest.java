package com.wishdiary.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
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
class EnglishContentManagementIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired org.springframework.jdbc.core.JdbcTemplate db;
    private static final String ADMIN = "/api/v1/admin/english/contents";

    @Test
    void databaseContentsCanBeAddedAndEditedWithoutChangingTodaysSelectedItem() throws Exception {
        JsonNode adult = signup("family"), child = signup("child");
        JsonNode today = call(get("/api/v1/english/today"), child, null, 200);
        long id = today.get("content").get("contentId").asLong();
        int before = call(get(ADMIN), adult, null, 200).get("contents").size();
        JsonNode added = call(post(ADMIN), adult, Map.of("sentence", " I like books. ", "meaning", " 나는 책을 좋아해요. "), 201);
        assertEquals("I like books.", added.get("sentence").asText());
        assertEquals(before + 1, call(get(ADMIN), adult, null, 200).get("contents").size());
        assertEquals(id, call(get("/api/v1/english/today"), child, null, 200).get("content").get("contentId").asLong());
        JsonNode changed = update(adult, id, today.get("content").get("version").asLong(), "I can sing.", "나는 노래할 수 있어요.", 200);
        assertEquals("I can sing.", call(get("/api/v1/english/today"), child, null, 200).get("content").get("sentence").asText());
        assertEquals(1, changed.get("version").asLong());
        assertEquals("나는 노래할 수 있어요.", db.queryForObject("select meaning from english_contents where id = ?", String.class, id));
        update(adult, id, 0, "Stale text", "이전 수정", 409);
        assertEquals("I can sing.", db.queryForObject("select sentence from english_contents where id = ?", String.class, id));
    }

    @Test
    void contentChangesRequireRefreshingBeforeRequestAndNeverRewriteExistingStudies() throws Exception {
        JsonNode adult = signup("family"), child = signup("child"); connect(child, adult);
        JsonNode before = call(get("/api/v1/english/today"), child, null, 200);
        long id = before.get("content").get("contentId").asLong();
        update(adult, id, before.get("content").get("version").asLong(), "I can swim.", "나는 수영할 수 있어요.", 200);
        request(child, adult, before, 409);
        JsonNode fresh = call(get("/api/v1/english/today"), child, null, 200);
        JsonNode study = request(child, adult, fresh, 200);
        update(adult, id, fresh.get("content").get("version").asLong(), "Let's run!", "함께 달리자!", 200);
        JsonNode stable = call(get("/api/v1/english/today"), child, null, 200);
        assertEquals("I can swim.", stable.get("content").get("sentence").asText());
        assertEquals("나는 수영할 수 있어요.", stable.get("content").get("meaning").asText());
        assertEquals("I can swim.", call(get("/api/v1/english/requests"), adult, null, 200).get(0).get("sentence").asText());
        long requestId = study.get("requestId").asLong();
        call(patch("/api/v1/english/requests/" + requestId), adult, Map.of("action", "REJECT"), 200);
        assertEquals(requestId, request(child, adult, stable, 200).get("requestId").asLong());
        call(patch("/api/v1/english/requests/" + requestId), adult, Map.of("action", "APPROVE"), 200);
        assertEquals("I can swim.", call(get("/api/v1/english/today"), child, null, 200).get("content").get("sentence").asText());
        assertEquals(1, call(get("/api/v1/clovers/balance"), child, null, 200).get("balance").asInt());
    }

    @Test
    void validationAndExistingLoginPolicyApplyToContentManagement() throws Exception {
        JsonNode user = signup("family");
        int before = call(get(ADMIN), user, null, 200).get("contents").size();
        for (var invalid : List.of(Map.of("sentence", "  ", "meaning", "뜻"), Map.of("sentence", "Hello", "meaning", "  "),
                Map.of("sentence", "x".repeat(121), "meaning", "뜻"))) {
            call(post(ADMIN), user, invalid, 400);
        }
        assertEquals(before, call(get(ADMIN), user, null, 200).get("contents").size());
        call(get(ADMIN), null, null, 401);
        call(post(ADMIN), null, Map.of("sentence", "Hello", "meaning", "안녕"), 401);
        update(user, Long.MAX_VALUE, 0, "Hello", "안녕", 404);
        JsonNode regularChild = signup("child");
        call(get(ADMIN), regularChild, null, 200); // ADMIN authorization remains deferred.
    }

    private JsonNode update(JsonNode user, long id, long version, String sentence, String meaning, int status) throws Exception {
        return call(patch(ADMIN + "/" + id), user, Map.of("sentence", sentence, "meaning", meaning, "version", version), status);
    }
    private JsonNode request(JsonNode child, JsonNode adult, JsonNode today, int status) throws Exception {
        var body = new HashMap<String,Object>();
        body.put("receiverFamilyUserId", adult.get("userId").asLong()); body.put("studyDate", today.get("studyDate").asText());
        body.put("contentId", today.get("content").get("contentId").isNull() ? null : today.get("content").get("contentId").asLong());
        body.put("contentVersion", today.get("content").get("version").isNull() ? null : today.get("content").get("version").asLong());
        return call(post("/api/v1/english/requests"), child, body, status);
    }
    private void connect(JsonNode child, JsonNode adult) throws Exception {
        var link = call(post("/api/v1/family-connections/requests"), adult, Map.of("childUserId", child.get("userId").asLong()), 201);
        call(patch("/api/v1/family-connections/requests/" + link.get("requestId").asLong()), child, Map.of("action", "APPROVE"), 200);
    }
    private JsonNode signup(String role) throws Exception {
        var body = new HashMap<String,Object>(Map.of("loginId", UUID.randomUUID().toString(), "password", "password", "nickname", "콘텐츠 확인"));
        if (role.equals("family")) { body.put("familyRelation", "AUNT"); body.put("relationLabel", "이모"); }
        return call(post("/api/v1/auth/signup/" + role), null, body, 201);
    }
    private JsonNode call(MockHttpServletRequestBuilder req, JsonNode user, Object body, int expected) throws Exception {
        if (user != null) req.header("Authorization", "Bearer " + user.get("accessToken").asText());
        if (body != null) req.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        var res = mvc.perform(req).andReturn().getResponse();
        assertEquals(expected, res.getStatus(), res.getContentAsString());
        return json.readTree(res.getContentAsByteArray()).get("data");
    }
}
