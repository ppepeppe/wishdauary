package com.wishdiary.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
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
class GiftReceiptIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate database;
    private record Fixture(JsonNode child, JsonNode family, long requestId) { }

    @Test
    void receiptPersistsForBothSidesWithoutChangingCloversApprovalOrPhotos() throws Exception {
        Fixture f = fixture();
        process(f, "APPROVE", 200);
        JsonNode approved = call(get("/api/v1/shop/gift-requests/sent"), f.child(), null, 200).get(0);
        JsonNode received = receive(f.child(), f.requestId(), 200);
        assertEquals("received", received.get("status").asText());
        assertFalse(received.get("receivedAt").isNull());
        assertEquals(approved.get("processedAt"), received.get("processedAt"));
        assertEquals(approved.get("processedByFamilyUserId"), received.get("processedByFamilyUserId"));
        assertEquals(approved.get("productImageUrl"), received.get("productImageUrl"));
        assertTrue(received.get("productImageUrl").asText().startsWith("data:image/jpeg;base64,"));
        JsonNode repeated = receive(f.child(), f.requestId(), 200);
        assertEquals(received.get("receivedAt"), repeated.get("receivedAt"));
        for (JsonNode user : new JsonNode[]{f.child(), f.family()}) {
            String path = user == f.child() ? "/sent" : "";
            JsonNode history = call(get("/api/v1/shop/gift-requests" + path), user, null, 200).get(0);
            assertEquals("received", history.get("status").asText());
            assertEquals(received.get("receivedAt"), history.get("receivedAt"));
        }
        assertNotNull(database.queryForObject("select received_at from gift_request_notifications where id=?", java.sql.Timestamp.class, f.requestId()));
        assertEquals(0, balance(f.child()));
        assertEquals(1, ledgerCount(f, "GIFT_REQUEST"));
        assertEquals(0, ledgerCount(f, "GIFT_REQUEST_REFUND"));
        process(f, "REJECT", 409);
        process(f, "APPROVE", 409);
        assertEquals(0, balance(f.child()));
    }

    @Test
    void onlyTheRequestingChildCanReceiveAnApprovedGift() throws Exception {
        Fixture f = fixture();
        receive(f.child(), f.requestId(), 409);
        receive(f.family(), f.requestId(), 403);
        receive(null, f.requestId(), 401);
        process(f, "APPROVE", 200);
        receive(signup("child"), f.requestId(), 403);
        receive(f.child(), Long.MAX_VALUE, 404);
        assertEquals("APPROVED", database.queryForObject("select status from gift_request_notifications where id=?", String.class, f.requestId()));
        assertNull(database.queryForObject("select received_at from gift_request_notifications where id=?", java.sql.Timestamp.class, f.requestId()));
        assertEquals(0, balance(f.child()));
    }

    @Test
    void rejectedGiftsCannotBeMarkedReceivedOrLoseTheirRefund() throws Exception {
        Fixture f = fixture();
        process(f, "REJECT", 200);
        receive(f.child(), f.requestId(), 409);
        assertEquals(1, balance(f.child()));
        assertEquals(1, ledgerCount(f, "GIFT_REQUEST_REFUND"));
    }

    @Test
    void simultaneousReceiptConfirmationsKeepOneReceiptDateAndNoExtraCharge() throws Exception {
        Fixture f = fixture();
        process(f, "APPROVE", 200);
        CyclicBarrier start = new CyclicBarrier(6);
        try (var pool = Executors.newFixedThreadPool(6)) {
            var futures = new ArrayList<java.util.concurrent.Future<JsonNode>>();
            for (int i = 0; i < 6; i++) futures.add(pool.submit(() -> {
                start.await(10, TimeUnit.SECONDS);
                return receive(f.child(), f.requestId(), 200);
            }));
            String receivedAt = futures.get(0).get(20, TimeUnit.SECONDS).get("receivedAt").asText();
            for (var future : futures) {
                JsonNode response = future.get(20, TimeUnit.SECONDS);
                assertEquals("received", response.get("status").asText());
                assertEquals(receivedAt, response.get("receivedAt").asText());
            }
        }
        assertEquals(0, balance(f.child()));
        assertEquals(1, ledgerCount(f, "GIFT_REQUEST"));
        assertEquals(0, ledgerCount(f, "GIFT_REQUEST_REFUND"));
    }

    private Fixture fixture() throws Exception {
        JsonNode child = signup("child"), family = signup("family");
        JsonNode link = call(post("/api/v1/family-connections/requests"), family, Map.of("childUserId", id(child)), 201);
        call(patch("/api/v1/family-connections/requests/" + link.get("requestId").asLong()), child, Map.of("action", "APPROVE"), 200);
        JsonNode product = call(post("/api/v1/shop/wish-requests"), child, Map.of("title", "수령 확인 선물", "description", "받고 싶어요",
                "receiverFamilyUserId", id(family), "imageData", photo()), 201);
        long productId = product.get("productId").asLong();
        call(patch("/api/v1/shop/products/" + productId + "/price"), family, Map.of("cloverPrice", 1), 200);
        call(post("/api/v1/diaries"), child, Map.of("content", "오늘 기록", "visibilityScope", "private", "writtenDate", LocalDate.now().toString()), 201);
        long requestId = call(post("/api/v1/shop/gift-requests"), child,
                Map.of("productId", productId, "receiverFamilyUserId", id(family), "message", "부탁해요"), 201).get("notificationId").asLong();
        return new Fixture(child, family, requestId);
    }

    private JsonNode signup(String role) throws Exception {
        var body = new java.util.HashMap<String, Object>(Map.of("loginId", UUID.randomUUID().toString(), "password", "password", "nickname", TestNicknames.unique("수령 확인")));
        if (role.equals("family")) { body.put("familyRelation", "AUNT"); body.put("relationLabel", "고모"); }
        return call(post("/api/v1/auth/signup/" + role), null, body, 201);
    }

    private String photo() throws Exception {
        var bytes = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(24, 16, java.awt.image.BufferedImage.TYPE_INT_RGB), "png", bytes);
        return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(bytes.toByteArray());
    }

    private long id(JsonNode user) { return user.get("userId").asLong(); }
    private int balance(JsonNode child) throws Exception { return call(get("/api/v1/clovers/balance"), child, null, 200).get("balance").asInt(); }
    private int ledgerCount(Fixture f, String reason) {
        return database.queryForObject("select count(*) from clover_ledgers where user_id=? and reason=? and ref_id=?", Integer.class, id(f.child()), reason, f.requestId());
    }
    private JsonNode receive(JsonNode user, long requestId, int status) throws Exception {
        return call(patch("/api/v1/shop/gift-requests/" + requestId + "/receipt"), user, null, status);
    }
    private JsonNode process(Fixture f, String action, int status) throws Exception {
        return call(patch("/api/v1/shop/gift-requests/" + f.requestId()), f.family(), Map.of("action", action), status);
    }
    private JsonNode call(MockHttpServletRequestBuilder request, JsonNode user, Object body, int expected) throws Exception {
        if (user != null) request.header("Authorization", "Bearer " + user.get("accessToken").asText());
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        var response = mvc.perform(request).andReturn().getResponse();
        assertEquals(expected, response.getStatus(), response.getContentAsString());
        return json.readTree(response.getContentAsByteArray()).get("data");
    }
}
