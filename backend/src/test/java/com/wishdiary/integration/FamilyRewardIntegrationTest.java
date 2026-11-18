package com.wishdiary.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FamilyRewardIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired org.springframework.jdbc.core.JdbcTemplate database;

    @Test
    void childProfileRequiresConnectionAndRefreshesAwardHistory() throws Exception {
        JsonNode child = signup("child"), adult = signup("family"), stranger = signup("family");
        String path = "/api/v1/family/children/" + child.get("userId").asLong();
        call(get(path), null, null, 401);
        call(get(path), adult, null, 403);
        call(get(path), child, null, 403);
        connect(child, adult);
        JsonNode profile = call(get(path), adult, null, 200);
        assertEquals(child.get("userId"), profile.get("childId"));
        assertEquals(0, profile.get("clovers").get("balance").asInt());
        assertTrue(profile.get("giftRequests").isArray());
        call(post("/api/v1/clovers/awards"), adult, award(child, 7, "스스로 정리했어요"), 200);
        profile = call(get(path), adult, null, 200);
        assertEquals(7, profile.get("clovers").get("balance").asInt());
        assertEquals("스스로 정리했어요", profile.get("history").get("entries").get(0).get("note").asText());
        long productId = call(post("/api/v1/shop/products"), child,
                Map.of("title", "프로필 테스트 선물", "description", "선물 내역 확인", "productUrl", "https://example.com/gift"), 201).get("productId").asLong();
        call(patch("/api/v1/shop/products/" + productId + "/price"), adult,
                Map.of("cloverPrice", 1, "purchasePriceWon", 1000), 200);
        call(post("/api/v1/shop/gift-requests"), child, Map.of("productId", productId,
                "receiverFamilyUserId", adult.get("userId").asLong(), "message", "부탁해요"), 201);
        profile = call(get(path), adult, null, 200);
        assertEquals(6, profile.get("clovers").get("balance").asInt());
        assertEquals("프로필 테스트 선물", profile.get("giftRequests").get(0).get("productTitle").asText());
        assertEquals("pending", profile.get("giftRequests").get(0).get("status").asText());
        assertFalse(profile.has("loginId"));
        call(get(path), stranger, null, 403);
        long groupId = call(get("/api/v1/sharing-groups"), child, null, 200).get(0).get("id").asLong();
        call(delete("/api/v1/sharing-groups/" + groupId), child, null, 200);
        call(get(path), adult, null, 403);
    }

    @Test
    void familyAwardsRequireAConnectionAndPreserveTheReasonWithoutDuplicateCredits() throws Exception {
        JsonNode child = signup("child"), adult = signup("family"), stranger = signup("family");
        connect(child, adult);
        var body = award(child, 5, " 친구를 도와줬어요 ");
        call(post("/api/v1/clovers/awards"), stranger, body, 403);
        call(post("/api/v1/clovers/awards"), child, body, 403);
        JsonNode first = call(post("/api/v1/clovers/awards"), adult, body, 200);
        JsonNode retry = call(post("/api/v1/clovers/awards"), adult, body, 200);
        assertEquals(first.get("ledgerId"), retry.get("ledgerId"));
        assertEquals(5, balance(child));
        var changed = new HashMap<String,Object>(body); changed.put("amount", 6);
        call(post("/api/v1/clovers/awards"), adult, changed, 409);
        JsonNode entry = call(get("/api/v1/clovers/history"), child, null, 200).get("entries").get(0);
        assertEquals("FAMILY_AWARD", entry.get("reason").asText());
        assertEquals("친구를 도와줬어요", entry.get("note").asText());
        assertEquals("이모", entry.get("givenByName").asText());
        for (Object invalid : new Object[]{0, -1, 1001, 1.5}) {
            var bad = award(child, 1, "칭찬"); bad.put("amount", invalid);
            call(post("/api/v1/clovers/awards"), adult, bad, 400);
        }
        call(post("/api/v1/clovers/awards"), adult, award(child, 1, "  "), 400);
        call(post("/api/v1/clovers/awards"), null, body, 401);
        assertEquals(5, balance(child));
    }

    @Test
    void simultaneousAwardRetriesGrantOnlyOnce() throws Exception {
        JsonNode child = signup("child"), adult = signup("family"); connect(child, adult);
        var body = award(child, 3, "정리를 잘했어요");
        runTogether(() -> call(post("/api/v1/clovers/awards"), adult, body, 200));
        assertEquals(3, balance(child));
    }

    @Test
    void englishRequiresTheSelectedAdultAndAwardsOneCloverOncePerStudyDay() throws Exception {
        JsonNode child = signup("child"), adult = signup("family"), other = signup("family");
        connect(child, adult); connect(child, other);
        JsonNode today = call(get("/api/v1/english/today"), child, null, 200);
        assertFalse(today.get("content").get("sentence").asText().isBlank());
        assertFalse(today.get("content").get("meaning").asText().isBlank());
        JsonNode requested = request(child, adult);
        assertEquals(today.get("content").get("sentence"), requested.get("sentence"));
        assertEquals(0, balance(child));
        long requestId = requested.get("requestId").asLong();
        runTogether(() -> request(child, adult));
        review(other, requestId, "APPROVE", 403);
        review(child, requestId, "APPROVE", 403);
        assertEquals(0, call(get("/api/v1/english/requests"), other, null, 200).size());
        assertEquals(1, call(get("/api/v1/english/requests"), adult, null, 200).size());
        runTogether(() -> review(adult, requestId, "APPROVE", 200));
        assertEquals(1, balance(child));
        review(adult, requestId, "REJECT", 409);
        assertEquals("approved", request(child, other).get("status").asText());
        assertEquals("approved", call(get("/api/v1/english/today"), child, null, 200).get("request").get("status").asText());
        assertEquals(0, call(get("/api/v1/english/requests"), adult, null, 200).size());
        JsonNode entry = call(get("/api/v1/clovers/history"), child, null, 200).get("entries").get(0);
        assertEquals("ENGLISH_STUDY", entry.get("reason").asText());
        assertEquals("이모", entry.get("givenByName").asText());
        assertTrue(entry.get("note").asText().contains(today.get("content").get("sentence").asText()));
    }

    @Test
    void rejectedStudyCanBeRequestedAgainAndYesterdayCanBeConfirmedWithoutUsingTodaysReward() throws Exception {
        JsonNode child = signup("child"), adult = signup("family"), other = signup("family");
        connect(child, adult); connect(child, other);
        long id = request(child, adult).get("requestId").asLong();
        call(post("/api/v1/english/requests"), child, Map.of("receiverFamilyUserId", other.get("userId").asLong(), "studyDate", todayDate()), 409);
        review(adult, id, "REJECT", 200); assertEquals(0, balance(child));
        assertEquals(id, request(child, other).get("requestId").asLong());
        review(adult, id, "APPROVE", 403);
        database.update("update english_studies set study_date = ? where id = ?",
                java.time.LocalDate.now(java.time.ZoneId.of("Asia/Seoul")).minusDays(1), id);
        review(other, id, "APPROVE", 200);
        long next = request(child, adult).get("requestId").asLong(); assertNotEquals(id, next);
        review(adult, next, "APPROVE", 200); assertEquals(2, balance(child));
    }

    @Test
    void englishRejectsUnconnectedReceiversAndDoesNotPayAfterFamilyDisconnection() throws Exception {
        JsonNode child = signup("child"), adult = signup("family");
        call(post("/api/v1/english/requests"), child, Map.of("receiverFamilyUserId", adult.get("userId").asLong(), "studyDate", todayDate()), 403);
        call(get("/api/v1/english/today"), adult, null, 403);
        call(get("/api/v1/english/requests"), child, null, 403);
        call(get("/api/v1/english/today"), null, null, 401);
        connect(child, adult);
        long id = request(child, adult).get("requestId").asLong();
        JsonNode group = call(get("/api/v1/sharing-groups"), child, null, 200).get(0);
        call(delete("/api/v1/sharing-groups/" + group.get("id").asLong()), child, null, 200);
        review(adult, id, "APPROVE", 403); assertEquals(0, balance(child));
    }

    @Test
    void staleEnglishDatesCannotReceiveTodaysReward() throws Exception {
        JsonNode child = signup("child"), adult = signup("family"); connect(child, adult);
        call(post("/api/v1/english/requests"), child,
                Map.of("receiverFamilyUserId", adult.get("userId").asLong(), "studyDate", "2020-01-01"), 409);
        assertTrue(call(get("/api/v1/english/today"), child, null, 200).get("request").isNull());
        assertEquals(0, balance(child));
    }

    private String todayDate() { return java.time.LocalDate.now(java.time.ZoneId.of("Asia/Seoul")).toString(); }

    private Map<String,Object> award(JsonNode child, int amount, String reason) {
        return new HashMap<>(Map.of("childUserId", child.get("userId").asLong(), "amount", amount,
                "reason", reason, "requestKey", UUID.randomUUID().toString()));
    }
    private JsonNode request(JsonNode child, JsonNode adult) throws Exception {
        JsonNode today = call(get("/api/v1/english/today"), child, null, 200);
        var body = new HashMap<String,Object>();
        body.put("receiverFamilyUserId", adult.get("userId").asLong()); body.put("studyDate", today.get("studyDate").asText());
        body.put("contentId", today.get("content").get("contentId").isNull() ? null : today.get("content").get("contentId").asLong());
        body.put("contentVersion", today.get("content").get("version").isNull() ? null : today.get("content").get("version").asLong());
        return call(post("/api/v1/english/requests"), child, body, 200);
    }
    private JsonNode review(JsonNode adult, long id, String action, int status) throws Exception {
        return call(patch("/api/v1/english/requests/" + id), adult, Map.of("action", action), status);
    }
    private int balance(JsonNode child) throws Exception {
        return call(get("/api/v1/clovers/balance"), child, null, 200).get("balance").asInt();
    }
    private void runTogether(Callable<JsonNode> action) throws Exception {
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(6)) {
            var tasks = new ArrayList<Future<JsonNode>>();
            for (int i = 0; i < 6; i++) tasks.add(pool.submit(() -> { start.await(); return action.call(); }));
            start.countDown();
            for (var task : tasks) task.get(20, TimeUnit.SECONDS);
        }
    }
    private void connect(JsonNode child, JsonNode adult) throws Exception {
        JsonNode link = call(post("/api/v1/family-connections/requests"), adult, Map.of("childUserId", child.get("userId").asLong()), 201);
        call(patch("/api/v1/family-connections/requests/" + link.get("requestId").asLong()), child, Map.of("action", "APPROVE"), 200);
    }
    private JsonNode signup(String role) throws Exception {
        var body = new HashMap<String,Object>(Map.of("loginId", UUID.randomUUID().toString(), "password", "password", "nickname", role.equals("child") ? "서아" : "이모"));
        if (role.equals("family")) { body.put("familyRelation", "AUNT"); body.put("relationLabel", "이모"); }
        return call(post("/api/v1/auth/signup/" + role), null, body, 201);
    }
    private JsonNode call(MockHttpServletRequestBuilder request, JsonNode user, Object body, int expected) throws Exception {
        if (user != null) request.header("Authorization", "Bearer " + user.get("accessToken").asText());
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        var result = mvc.perform(request).andReturn().getResponse();
        assertEquals(expected, result.getStatus(), result.getContentAsString());
        return json.readTree(result.getContentAsByteArray()).get("data");
    }
}
