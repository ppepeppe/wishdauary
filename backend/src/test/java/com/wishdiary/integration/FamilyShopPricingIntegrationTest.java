package com.wishdiary.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
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
class FamilyShopPricingIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    private static final String MANAGED = "/api/v1/shop/managed-products";

    @Test
    void managementKeepsChildAndFamilyProductsVisibleAfterPricing() throws Exception {
        JsonNode child = signup("child"), other = signup("child"), family = signup("family");
        long custom = customProduct(child), hidden = customProduct(other);
        assertEquals(0, call(get(MANAGED), family, null, 200).size());
        connect(child, family);
        long uploaded = call(post("/api/v1/shop/family-products"), family,
                Map.of("childUserId", id(child), "title", "가족 선물", "description", "함께 골라요", "cloverPrice", 3), 201)
                .get("productId").asLong();
        price(family, custom, 1, 200);
        price(family, custom, 5, 200);
        price(family, uploaded, 7, 200);
        JsonNode managed = call(get(MANAGED), family, null, 200);
        assertEquals(2, managed.size());
        assertEquals(5, find(managed, custom).get("cloverPrice").asInt());
        assertEquals(7, find(managed, uploaded).get("cloverPrice").asInt());
        assertNull(find(managed, hidden));
        assertEquals(0, call(get("/api/v1/shop/product-pricing-requests"), family, null, 200).size());
        assertEquals(1, call(get("/api/v1/shop/family-products"), family, null, 200).size());
        call(get(MANAGED), child, null, 403);
        call(get(MANAGED), null, null, 401);
    }

    @Test
    void unrelatedFamiliesChildrenAndInvalidPricesCannotChangeProducts() throws Exception {
        JsonNode child = signup("child"), family = signup("family"), outsider = signup("family");
        long product = customProduct(child);
        connect(child, family);
        price(outsider, product, 3, 403);
        price(child, product, 3, 403);
        price(family, product, 0, 400);
        price(family, product, -1, 400);
        price(family, product, 4, 200);
        JsonNode group = call(get("/api/v1/sharing-groups"), child, null, 200).get(0);
        call(delete("/api/v1/sharing-groups/" + group.get("id").asLong()), child, null, 200);
        assertEquals(0, call(get(MANAGED), family, null, 200).size());
        price(family, product, 8, 403);
    }

    @Test
    void priceChangesLeaveSentRequestsAndTheirRefundAmountsUnchanged() throws Exception {
        JsonNode child = signup("child"), family = signup("family");
        connect(child, family);
        call(post("/api/v1/diaries"), child, Map.of("content", "오늘 기록", "visibilityScope", "family",
                "writtenDate", LocalDate.now().toString()), 201);
        long product = customProduct(child);
        price(family, product, 1, 200);
        JsonNode gift = call(post("/api/v1/shop/gift-requests"), child,
                Map.of("productId", product, "receiverFamilyUserId", id(family), "message", "부탁해요"), 201);
        price(family, product, 10, 200);
        assertEquals(1, call(get("/api/v1/shop/gift-requests"), family, null, 200).get(0).get("cloverPrice").asInt());
        JsonNode rejected = call(patch("/api/v1/shop/gift-requests/" + gift.get("notificationId").asLong()),
                family, Map.of("action", "REJECT"), 200);
        assertEquals(1, rejected.get("cloverPrice").asInt());
        JsonNode summary = call(get("/api/v1/users/home-summary").param("date", LocalDate.now().toString()), child, null, 200);
        assertEquals(1, summary.get("cloverBalance").asDouble());
    }

    private JsonNode find(JsonNode products, long id) {
        for (JsonNode product : products) if (product.get("productId").asLong() == id) return product;
        return null;
    }

    private long customProduct(JsonNode child) throws Exception {
        return call(post("/api/v1/shop/products"), child,
                Map.of("title", "아이가 고른 책", "description", "읽고 싶어요"), 201).get("productId").asLong();
    }

    private JsonNode price(JsonNode family, long productId, int price, int expected) throws Exception {
        return call(patch("/api/v1/shop/products/" + productId + "/price"), family,
                Map.of("cloverPrice", price, "purchasePriceWon", 12000), expected);
    }

    private void connect(JsonNode child, JsonNode family) throws Exception {
        JsonNode request = call(post("/api/v1/family-connections/requests"), family, Map.of("childUserId", id(child)), 201);
        call(patch("/api/v1/family-connections/requests/" + request.get("requestId").asLong()), child, Map.of("action", "APPROVE"), 200);
    }

    private JsonNode signup(String role) throws Exception {
        var body = new java.util.HashMap<String, Object>(Map.of("email", UUID.randomUUID() + "@test.com",
                "password", "password", "nickname", "테스트"));
        if (role.equals("family")) { body.put("familyRelation", "MOTHER"); body.put("relationLabel", "엄마"); }
        return call(post("/api/v1/auth/signup/" + role), null, body, 201);
    }

    private long id(JsonNode user) { return user.get("userId").asLong(); }

    private JsonNode call(MockHttpServletRequestBuilder request, JsonNode user, Object body, int expected) throws Exception {
        if (user != null) request.header("Authorization", "Bearer " + user.get("accessToken").asText());
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        var response = mvc.perform(request).andReturn().getResponse();
        assertEquals(expected, response.getStatus(), response.getContentAsString());
        return json.readTree(response.getContentAsByteArray()).get("data");
    }
}
