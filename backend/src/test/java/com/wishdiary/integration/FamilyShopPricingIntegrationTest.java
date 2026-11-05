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
    @Autowired org.springframework.jdbc.core.JdbcTemplate database;
    @Autowired jakarta.persistence.EntityManager entities;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired com.wishdiary.application.wishgoal.SaveWishGoalPort goals;
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

    @Test
    void siblingsShareTheCatalogButTheirWishProductsStaySeparate() throws Exception {
        JsonNode first = signup("child"), second = signup("child"), family = signup("family");
        connect(first, family);
        connect(second, family);
        long firstWish = customProduct(first), secondWish = customProduct(second);
        price(family, firstWish, 1, 200);
        price(family, secondWish, 1, 200);
        long familyGift = call(post("/api/v1/shop/family-products"), family,
                Map.of("childUserId", id(first), "title", "첫째 선물", "description", "함께 골라요", "cloverPrice", 3), 201)
                .get("productId").asLong();
        JsonNode firstProducts = call(get("/api/v1/shop/products"), first, null, 200);
        JsonNode secondProducts = call(get("/api/v1/shop/products"), second, null, 200);
        var shared = new java.util.ArrayList<Long>();
        for (JsonNode product : firstProducts) {
            if (product.get("source").asText().equals("preset")) shared.add(product.get("productId").asLong());
        }
        assertFalse(shared.isEmpty());
        for (long productId : shared) assertNotNull(find(secondProducts, productId));
        assertNotNull(find(firstProducts, firstWish));
        assertNotNull(find(firstProducts, familyGift));
        assertNull(find(firstProducts, secondWish));
        assertNotNull(find(secondProducts, secondWish));
        assertNull(find(secondProducts, firstWish));
        assertNull(find(secondProducts, familyGift));
        JsonNode forged = call(get("/api/v1/shop/products").param("childUserId", "" + id(first)), second, null, 200);
        assertNull(find(forged, firstWish));
        call(post("/api/v1/shop/gift-requests"), second,
                Map.of("productId", firstWish, "receiverFamilyUserId", id(family), "message", "다른 아이 상품"), 404);
    }

    @Test
    void sentGiftRequestsBelongOnlyToTheAuthenticatedChild() throws Exception {
        JsonNode first = signup("child"), second = signup("child"), family = signup("family");
        connect(first, family);
        connect(second, family);
        JsonNode firstRequest = sendGift(first, family);
        JsonNode secondRequest = sendGift(second, family);
        JsonNode firstHistory = call(get("/api/v1/shop/gift-requests/sent"), first, null, 200);
        JsonNode secondHistory = call(get("/api/v1/shop/gift-requests/sent")
                .param("childUserId", "" + id(first)), second, null, 200);
        assertEquals(1, firstHistory.size());
        assertEquals(1, secondHistory.size());
        assertEquals(firstRequest.get("notificationId"), firstHistory.get(0).get("notificationId"));
        assertEquals(secondRequest.get("notificationId"), secondHistory.get(0).get("notificationId"));
        assertNotEquals(firstHistory.get(0).get("notificationId"), secondHistory.get(0).get("notificationId"));
        assertEquals(2, call(get("/api/v1/shop/gift-requests"), family, null, 200).size());
    }

    private JsonNode sendGift(JsonNode child, JsonNode family) throws Exception {
        call(post("/api/v1/diaries"), child, Map.of("content", "오늘 기록", "visibilityScope", "family",
                "writtenDate", LocalDate.now().toString()), 201);
        long product = customProduct(child);
        price(family, product, 1, 200);
        return call(post("/api/v1/shop/gift-requests"), child,
                Map.of("productId", product, "receiverFamilyUserId", id(family), "message", "부탁해요"), 201);
    }

    @Test
    void wishRequestsNeedTheChosenFamilyMembersPriceBeforePublication() throws Exception {
        JsonNode child = signup("child"), other = signup("child"), mother = signup("family"), father = signup("family");
        connect(child, mother);
        connect(child, father);
        JsonNode wish = call(post("/api/v1/shop/wish-requests"), child, Map.of("title", "새 자전거", "description", "타고 싶어요",
                "receiverFamilyUserId", id(mother), "cloverPrice", 999), 201);
        long productId = wish.get("productId").asLong();
        assertEquals(0, wish.get("cloverPrice").asInt());
        assertEquals(id(mother), wish.get("receiverFamilyUserId").asLong());
        assertNull(find(call(get("/api/v1/shop/products"), child, null, 200), productId));
        call(post("/api/v1/shop/gift-requests"), child, Map.of("productId", productId,
                "receiverFamilyUserId", id(mother), "message", "클로버 확인 전"), 400);
        assertEquals(1, call(get("/api/v1/shop/wish-requests"), child, null, 200).size());
        assertEquals(0, call(get("/api/v1/shop/wish-requests"), other, null, 200).size());
        call(get("/api/v1/shop/wish-requests"), mother, null, 403);
        assertNotNull(find(call(get("/api/v1/shop/product-pricing-requests"), mother, null, 200), productId));
        assertNull(find(call(get("/api/v1/shop/product-pricing-requests"), father, null, 200), productId));
        assertNull(find(call(get(MANAGED), father, null, 200), productId));
        price(father, productId, 5, 403);
        price(child, productId, 5, 403);
        price(mother, productId, 0, 400);
        price(mother, productId, 5, 200);
        assertEquals(5, find(call(get("/api/v1/shop/products"), child, null, 200), productId).get("cloverPrice").asInt());
        assertEquals(id(mother), call(get("/api/v1/shop/wish-requests"), child, null, 200).get(0).get("receiverFamilyUserId").asLong());
        assertEquals(0, call(get("/api/v1/clovers/balance"), child, null, 200).get("balance").asInt());
        call(post("/api/v1/shop/products"), child, Map.of("title", "자체 승인", "description", "불가", "cloverPrice", 9), 400);
    }

    @Test
    void wishRequestRecipientsMustBeChosenFromConnectedFamilies() throws Exception {
        JsonNode child = signup("child"), unconnected = signup("family"), otherChild = signup("child");
        call(post("/api/v1/shop/wish-requests"), child, Map.of("title", "책", "description", "읽고 싶어요"), 400);
        call(post("/api/v1/shop/wish-requests"), child, Map.of("title", "책", "description", "읽고 싶어요",
                "receiverFamilyUserId", id(unconnected)), 403);
        call(post("/api/v1/shop/wish-requests"), child, Map.of("title", "책", "description", "읽고 싶어요",
                "receiverFamilyUserId", id(otherChild)), 403);
    }

    @Test
    void personalWishGoalsCannotExposeAnotherChildsProduct() throws Exception {
        JsonNode first = signup("child"), second = signup("child"), family = signup("family");
        connect(first, family);
        connect(second, family);
        long productId = customProduct(first);
        price(family, productId, 5, 200);
        call(put("/api/v1/wish-goals/me"), second, Map.of("productId", productId), 404);
        goals.save(com.wishdiary.domain.wishgoal.WishGoal.create(id(second), productId));
        assertTrue(call(get("/api/v1/wish-goals/me"), second, null, 200).isNull());
        assertEquals(0, call(get("/api/v1/wish-goals/children"), family, null, 200).size());
    }

    @Test
    void uploadedPhotosPersistThroughPricingAndGiftProcessingWithPrivateAccess() throws Exception {
        JsonNode child = signup("child"), sibling = signup("child"), mother = signup("family"), father = signup("family");
        connect(child, mother);
        connect(child, father);
        JsonNode wish = call(post("/api/v1/shop/wish-requests"), child, Map.of("title", "사진 선물", "description", "이걸 원해요",
                "receiverFamilyUserId", id(mother), "imageData", photo()), 201);
        long productId = wish.get("productId").asLong();
        String imageUrl = wish.get("imageUrl").asText();
        assertTrue(imageUrl.startsWith("data:image/jpeg;base64,"));
        entities.flush(); entities.clear();
        byte[] stored = database.queryForObject("select image_data from shop_products where id = ?", byte[].class, productId);
        assertArrayEquals(java.util.Base64.getDecoder().decode(imageUrl.split(",")[1]), stored);
        assertEquals(24, javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(stored)).getWidth());
        assertEquals(imageUrl, call(get("/api/v1/shop/wish-requests"), child, null, 200).get(0).get("imageUrl").asText());
        assertEquals(imageUrl, find(call(get(MANAGED), mother, null, 200), productId).get("imageUrl").asText());
        assertNull(find(call(get(MANAGED), father, null, 200), productId));
        assertEquals(0, call(get("/api/v1/shop/wish-requests"), sibling, null, 200).size());
        call(get("/api/v1/shop/wish-requests"), null, null, 401);
        assertEquals(imageUrl, price(mother, productId, 1, 200).get("imageUrl").asText());
        entities.flush(); entities.clear();
        assertEquals(imageUrl, find(call(get("/api/v1/shop/products"), child, null, 200), productId).get("imageUrl").asText());
        call(post("/api/v1/diaries"), child, Map.of("content", "오늘 기록", "visibilityScope", "family",
                "writtenDate", LocalDate.now().toString()), 201);
        long requestId = call(post("/api/v1/shop/gift-requests"), child, Map.of("productId", productId,
                "receiverFamilyUserId", id(mother), "message", "부탁해요"), 201).get("notificationId").asLong();
        entities.flush(); entities.clear();
        assertArrayEquals(stored, database.queryForObject("select product_image_data from gift_request_notifications where id = ?", byte[].class, requestId));
        assertEquals(imageUrl, call(get("/api/v1/shop/gift-requests"), mother, null, 200).get(0).get("productImageUrl").asText());
        assertEquals(imageUrl, call(patch("/api/v1/shop/gift-requests/" + requestId), mother,
                Map.of("action", "REJECT"), 200).get("productImageUrl").asText());
        entities.flush(); entities.clear();
        assertEquals(imageUrl, call(get("/api/v1/shop/gift-requests/sent"), child, null, 200).get(0).get("productImageUrl").asText());
    }

    @Test
    void familyUploadsPhotosAndInvalidPhotosDoNotCreateProducts() throws Exception {
        JsonNode child = signup("child"), family = signup("family");
        connect(child, family);
        long before = database.queryForObject("select count(*) from shop_products", Long.class);
        for (String bad : new String[]{"data:image/jpeg;base64,aGVsbG8=", "data:image/svg+xml;base64,PHN2Zy8+", "A".repeat(700_001)}) {
            call(post("/api/v1/shop/wish-requests"), child, Map.of("title", "사진 선물", "description", "원해요",
                    "receiverFamilyUserId", id(family), "imageData", bad), 400);
        }
        assertEquals(before, database.queryForObject("select count(*) from shop_products", Long.class));
        JsonNode gift = call(post("/api/v1/shop/family-products"), family, Map.of("childUserId", id(child),
                "title", "가족 사진 선물", "description", "같이 골라요", "cloverPrice", 3, "imageData", photo()), 201);
        entities.flush(); entities.clear();
        assertEquals(gift.get("imageUrl"), find(call(get("/api/v1/shop/products"), child, null, 200), gift.get("productId").asLong()).get("imageUrl"));
    }

    private String photo() throws Exception {
        var bytes = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(24, 16, java.awt.image.BufferedImage.TYPE_INT_RGB), "png", bytes);
        return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(bytes.toByteArray());
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
