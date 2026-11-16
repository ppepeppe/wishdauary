package com.wishdiary.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GiftRedemptionConcurrencyIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate database;
    private record Result(int status, JsonNode data) { }
    private record Fixture(JsonNode child, JsonNode family, long productId) { }

    @Test
    void simultaneousRequestsCannotSpendTheSameCloverMoreThanOnce() throws Exception {
        Fixture f = fixture();
        List<Integer> statuses = concurrently(8, i -> () -> send(f).status());
        assertEquals(1, statuses.stream().filter(status -> status == 201).count(), statuses.toString());
        assertTrue(statuses.stream().allMatch(status -> status == 201 || status == 400), statuses.toString());
        assertEquals(0, balance(f.child()));
        assertEquals(1, database.queryForObject("select count(*) from clover_ledgers where user_id=? and reason='GIFT_REQUEST'",
                Integer.class, f.child().get("userId").asLong()));
    }

    @Test
    void simultaneousApprovalAndRejectionHaveOneFinalStateAndAtMostOneRefund() throws Exception {
        Fixture f = fixture();
        Result sent = send(f);
        assertEquals(201, sent.status());
        long requestId = sent.data().get("notificationId").asLong();
        List<Integer> statuses = concurrently(8, i -> () -> call(patch("/api/v1/shop/gift-requests/" + requestId),
                f.family(), Map.of("action", i % 2 == 0 ? "APPROVE" : "REJECT")).status());
        assertEquals(1, statuses.stream().filter(status -> status == 200).count(), statuses.toString());
        assertTrue(statuses.stream().allMatch(status -> status == 200 || status == 409), statuses.toString());
        String state = database.queryForObject("select status from gift_request_notifications where id=?", String.class, requestId);
        int refundCount = database.queryForObject("select count(*) from clover_ledgers where user_id=? and reason='GIFT_REQUEST_REFUND' and ref_id=?",
                Integer.class, f.child().get("userId").asLong(), requestId);
        assertEquals(state.equals("REJECTED") ? 1 : 0, refundCount);
        assertEquals(refundCount, balance(f.child()));
    }

    private Fixture fixture() throws Exception {
        JsonNode child = signup("child"), family = signup("family");
        JsonNode request = call(post("/api/v1/family-connections/requests"), family, Map.of("childUserId", child.get("userId").asLong())).data();
        assertEquals(200, call(patch("/api/v1/family-connections/requests/" + request.get("requestId").asLong()), child, Map.of("action", "APPROVE")).status());
        JsonNode product = call(post("/api/v1/shop/wish-requests"), child,
                Map.of("title", "원하는 책", "description", "읽고 싶어요", "receiverFamilyUserId", family.get("userId").asLong())).data();
        long productId = product.get("productId").asLong();
        assertEquals(200, call(patch("/api/v1/shop/products/" + productId + "/price"), family, Map.of("cloverPrice", 1)).status());
        assertEquals(201, call(post("/api/v1/diaries"), child,
                Map.of("content", "오늘의 기록", "visibilityScope", "private", "writtenDate", LocalDate.now().toString())).status());
        return new Fixture(child, family, productId);
    }

    private JsonNode signup(String role) throws Exception {
        var body = new java.util.HashMap<String, Object>(Map.of("loginId", UUID.randomUUID().toString(), "password", "password", "nickname", "동시 요청 점검"));
        if (role.equals("family")) { body.put("familyRelation", "MOTHER"); body.put("relationLabel", "엄마"); }
        Result result = call(post("/api/v1/auth/signup/" + role), null, body);
        assertEquals(201, result.status());
        return result.data();
    }

    private Result send(Fixture f) throws Exception {
        return call(post("/api/v1/shop/gift-requests"), f.child(), Map.of("productId", f.productId(),
                "receiverFamilyUserId", f.family().get("userId").asLong(), "message", "모은 클로버로 부탁해요"));
    }

    private int balance(JsonNode child) throws Exception {
        return call(get("/api/v1/clovers/balance"), child, null).data().get("balance").asInt();
    }

    private List<Integer> concurrently(int count, java.util.function.IntFunction<Callable<Integer>> task) throws Exception {
        CyclicBarrier start = new CyclicBarrier(count);
        try (var pool = Executors.newFixedThreadPool(count)) {
            var futures = new ArrayList<java.util.concurrent.Future<Integer>>();
            for (int i = 0; i < count; i++) {
                Callable<Integer> action = task.apply(i);
                futures.add(pool.submit(() -> { start.await(10, TimeUnit.SECONDS); return action.call(); }));
            }
            var results = new ArrayList<Integer>();
            for (var future : futures) results.add(future.get(20, TimeUnit.SECONDS));
            return results;
        }
    }

    private Result call(MockHttpServletRequestBuilder request, JsonNode user, Object body) throws Exception {
        if (user != null) request.header("Authorization", "Bearer " + user.get("accessToken").asText());
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        var response = mvc.perform(request).andReturn().getResponse();
        return new Result(response.getStatus(), json.readTree(response.getContentAsByteArray()).get("data"));
    }
}
