package com.wishdiary.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthAndShopFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    }

    @Test
    void signupAndValidationErrorsAreShownInKorean() {
        String email = "duplicate-" + System.nanoTime() + "@test.com";
        Map<String, String> signup = Map.of("email", email, "password", "password", "nickname", "아이");
        ResponseEntity<Map> first = restTemplate.postForEntity("/api/v1/auth/signup/child", signup, Map.class);
        assertEquals(HttpStatus.CREATED, first.getStatusCode());

        ResponseEntity<Map> duplicate = restTemplate.postForEntity("/api/v1/auth/signup/child", signup, Map.class);
        assertEquals(HttpStatus.CONFLICT, duplicate.getStatusCode());
        assertEquals("이미 가입된 이메일이에요.", duplicate.getBody().get("message"));

        ResponseEntity<Map> invalid = restTemplate.postForEntity(
                "/api/v1/auth/signup/child",
                Map.of("email", "not-an-email", "password", "password", "nickname", "아이"),
                Map.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, invalid.getStatusCode());
        assertEquals("이메일: 올바른 주소를 입력해 주세요.", invalid.getBody().get("message"));
    }

    @Test
    void authenticatedShopFlowPricesRequestsApprovesAndRejectsGiftRequests() {
        String suffix = String.valueOf(System.nanoTime());

        Map<String, Object> childSignup = post(
                "/api/v1/auth/signup/child",
                Map.of(
                        "email", "child-" + suffix + "@test.com",
                        "password", "password",
                        "nickname", "미아"
                )
        );
        Map<String, Object> child = data(childSignup);
        String childToken = string(child, "accessToken");

        Map<String, Object> familySignup = post(
                "/api/v1/auth/signup/family",
                Map.of(
                        "email", "family-" + suffix + "@test.com",
                        "password", "password",
                        "nickname", "엄마",
                        "familyRelation", "MOTHER",
                        "relationLabel", "엄마"
                )
        );
        Map<String, Object> family = data(familySignup);
        String familyToken = string(family, "accessToken");
        connectFamily(childToken, number(family, "userId").longValue());

        ResponseEntity<Map> unauthorizedResponse = restTemplate.getForEntity("/api/v1/shop/products", Map.class);
        assertEquals(HttpStatus.UNAUTHORIZED, unauthorizedResponse.getStatusCode());

        Map<String, Object> productResponse = post(
                "/api/v1/shop/products",
                childToken,
                Map.of(
                        "title", "포켓몬 카드",
                        "description", "쿠팡에서 본 카드",
                        "productUrl", "https://www.coupang.com/vp/products/" + suffix
                )
        );
        Map<String, Object> product = data(productResponse);
        Number productId = number(product, "productId");
        assertEquals(0, number(product, "cloverPrice").intValue());

        Map<String, Object> pricingResponse = exchange(
                HttpMethod.GET,
                "/api/v1/shop/product-pricing-requests",
                familyToken,
                null
        );
        List<Map<String, Object>> pricingRequests = dataList(pricingResponse);
        assertFalse(pricingRequests.isEmpty());

        Map<String, Object> pricedProductResponse = exchange(
                HttpMethod.PATCH,
                "/api/v1/shop/products/" + productId.longValue() + "/price",
                familyToken,
                Map.of(
                        "cloverPrice", 1,
                        "purchasePriceWon", 12900
                )
        );
        assertEquals(1, number(data(pricedProductResponse), "cloverPrice").intValue());
        assertEquals(12900, number(data(pricedProductResponse), "purchasePriceWon").intValue());

        writeDiary(childToken, "첫 번째 선물 요청을 위한 기록");
        Number familyUserId = firstRecipientId(childToken);

        Map<String, Object> giftRequestResponse = post(
                "/api/v1/shop/gift-requests",
                childToken,
                Map.of(
                        "productId", productId.longValue(),
                        "receiverFamilyUserId", familyUserId.longValue(),
                        "message", "갖고 싶어요"
                )
        );
        Map<String, Object> giftRequest = data(giftRequestResponse);
        Number notificationId = number(giftRequest, "notificationId");
        assertEquals("pending", string(giftRequest, "status"));
        assertEquals(0, number(giftRequest, "remainingCloverBalance").intValue());
        assertEquals(12900, number(giftRequest, "purchasePriceWon").intValue());

        Map<String, Object> approvedResponse = exchange(
                HttpMethod.PATCH,
                "/api/v1/shop/gift-requests/" + notificationId.longValue(),
                familyToken,
                Map.of("action", "APPROVE")
        );
        assertEquals("approved", string(data(approvedResponse), "status"));

        writeDiary(childToken, "두 번째 선물 요청을 위한 기록");
        Map<String, Object> secondGiftRequestResponse = post(
                "/api/v1/shop/gift-requests",
                childToken,
                Map.of(
                        "productId", productId.longValue(),
                        "receiverFamilyUserId", familyUserId.longValue(),
                        "message", "이번에는 거절 테스트"
                )
        );
        Number secondNotificationId = number(data(secondGiftRequestResponse), "notificationId");
        assertEquals(0, number(data(secondGiftRequestResponse), "remainingCloverBalance").intValue());
        Map<String, Object> rejectedResponse = exchange(
                HttpMethod.PATCH,
                "/api/v1/shop/gift-requests/" + secondNotificationId.longValue(),
                familyToken,
                Map.of("action", "REJECT")
        );
        assertEquals("rejected", string(data(rejectedResponse), "status"));

        Map<String, Object> balanceResponse = exchange(HttpMethod.GET, "/api/v1/clovers/balance", childToken, null);
        assertEquals(1, number(data(balanceResponse), "balance").intValue());

        Map<String, Object> receivedRequestsResponse = exchange(
                HttpMethod.GET,
                "/api/v1/shop/gift-requests",
                familyToken,
                null
        );
        List<Map<String, Object>> receivedRequests = dataList(receivedRequestsResponse);
        assertTrue(receivedRequests.stream().anyMatch(request -> "approved".equals(request.get("status"))));
        assertTrue(receivedRequests.stream().anyMatch(request -> "rejected".equals(request.get("status"))));

        Map<String, Object> sentRequestsResponse = exchange(
                HttpMethod.GET,
                "/api/v1/shop/gift-requests/sent",
                childToken,
                null
        );
        List<Map<String, Object>> sentRequests = dataList(sentRequestsResponse);
        assertTrue(sentRequests.stream().anyMatch(request -> "approved".equals(request.get("status"))));
        assertTrue(sentRequests.stream().anyMatch(request -> "rejected".equals(request.get("status"))));

        Map<String, Object> historyResponse = exchange(HttpMethod.GET, "/api/v1/clovers/history", childToken, null);
        List<Map<String, Object>> historyEntries = (List<Map<String, Object>>) data(historyResponse).get("entries");
        assertEquals(5, historyEntries.size());
        assertTrue(historyEntries.stream().anyMatch(entry -> "GIFT_REQUEST_REFUND".equals(entry.get("reason"))));
        assertTrue(historyEntries.stream().anyMatch(entry -> "DIARY_WRITE".equals(entry.get("reason"))));

        Map<String, Object> setGoalResponse = exchange(
                HttpMethod.PUT,
                "/api/v1/wish-goals/me",
                childToken,
                Map.of("productId", productId.longValue())
        );
        Map<String, Object> goal = data(setGoalResponse);
        assertEquals(productId.longValue(), number(goal, "productId").longValue());
        assertEquals(1, number(goal, "targetCloverPrice").intValue());
        assertEquals(1, number(goal, "currentCloverBalance").intValue());
        assertEquals(0, number(goal, "remainingClovers").intValue());

        Map<String, Object> getGoalResponse = exchange(HttpMethod.GET, "/api/v1/wish-goals/me", childToken, null);
        assertEquals(productId.longValue(), number(data(getGoalResponse), "productId").longValue());

        Map<String, Object> childrenGoalsResponse = exchange(HttpMethod.GET, "/api/v1/wish-goals/children", familyToken, null);
        List<Map<String, Object>> childrenGoals = dataList(childrenGoalsResponse);
        assertEquals(1, childrenGoals.size());
        assertEquals("미아", string(childrenGoals.get(0), "childNickname"));

        Map<String, Object> clearGoalResponse = exchange(HttpMethod.DELETE, "/api/v1/wish-goals/me", childToken, null);
        assertTrue((Boolean) clearGoalResponse.get("success"));

        ResponseEntity<Map> clearedGoalResponse = restTemplate.exchange(
                "/api/v1/wish-goals/me",
                HttpMethod.GET,
                jsonEntity(childToken, null),
                Map.class
        );
        assertTrue(clearedGoalResponse.getStatusCode().is2xxSuccessful());
        assertNull(clearedGoalResponse.getBody().get("data"));
    }

    @Test
    void defaultCatalogIsAvailableImmediatelyAndGiftRequestCanBeApproved() {
        String suffix = String.valueOf(System.nanoTime());
        Map<String, Object> child = data(post("/api/v1/auth/signup/child", Map.of(
                "email", "catalog-child-" + suffix + "@test.com", "password", "password", "nickname", "아이")));
        String childToken = string(child, "accessToken");
        Map<String, Object> family = data(post("/api/v1/auth/signup/family", Map.of(
                "email", "catalog-family-" + suffix + "@test.com", "password", "password", "nickname", "엄마",
                "familyRelation", "MOTHER", "relationLabel", "엄마")));
        String familyToken = string(family, "accessToken");
        connectFamily(childToken, number(family, "userId").longValue());

        List<Map<String, Object>> catalog = dataList(exchange(HttpMethod.GET, "/api/v1/shop/products", childToken, null));
        assertEquals(4, catalog.size());
        assertTrue(catalog.stream().allMatch(product -> "preset".equals(product.get("source"))));
        Map<String, Object> product = catalog.get(0);
        int price = number(product, "cloverPrice").intValue();
        assertTrue(price > 0);
        assertTrue(string(product, "imageUrl").startsWith("/assets/shop/"));
        for (int i = 0; i < price; i++) {
            writeDiary(childToken, "기본 선물을 위한 일기 " + i);
        }

        Map<String, Object> giftRequest = data(post("/api/v1/shop/gift-requests", childToken, Map.of(
                "productId", number(product, "productId").longValue(),
                "receiverFamilyUserId", number(family, "userId").longValue(), "message", "같이 골라주세요")));
        assertEquals("pending", string(giftRequest, "status"));
        assertEquals(0, number(giftRequest, "remainingCloverBalance").intValue());
        Map<String, Object> approved = data(exchange(HttpMethod.PATCH,
                "/api/v1/shop/gift-requests/" + number(giftRequest, "notificationId").longValue(),
                familyToken, Map.of("action", "APPROVE")));
        assertEquals("approved", string(approved, "status"));
        List<Map<String, Object>> sent = dataList(exchange(HttpMethod.GET, "/api/v1/shop/gift-requests/sent", childToken, null));
        assertEquals(1, sent.size());
        assertEquals(product.get("title"), sent.get(0).get("productTitle"));
    }

    private void connectFamily(String childToken, Long familyUserId) {
        post("/api/v1/sharing-groups", childToken, Map.of("name", "우리 가족", "memberIds", List.of(familyUserId)));
    }

    private void writeDiary(String childToken, String content) {
        Map<String, Object> response = post(
                "/api/v1/diaries",
                childToken,
                Map.of(
                        "content", content,
                        "emotion", "happy",
                        "visibilityScope", "family",
                        "writtenDate", LocalDate.now().toString()
                )
        );
        assertNotNull(data(response).get("diaryId"));
    }

    private Number firstRecipientId(String childToken) {
        Map<String, Object> response = exchange(HttpMethod.GET, "/api/v1/shop/recipients", childToken, null);
        List<Map<String, Object>> recipients = dataList(response);
        assertFalse(recipients.isEmpty());
        return number(recipients.get(0), "userId");
    }

    private Map<String, Object> post(String path, Map<String, Object> body) {
        ResponseEntity<Map> response = restTemplate.postForEntity(path, jsonEntity(null, body), Map.class);
        assertTrue(
                response.getStatusCode().is2xxSuccessful(),
                () -> "POST " + path + " failed: " + response.getStatusCode() + " body=" + response.getBody()
        );
        return response.getBody();
    }

    private Map<String, Object> post(String path, String token, Map<String, Object> body) {
        ResponseEntity<Map> response = restTemplate.postForEntity(path, jsonEntity(token, body), Map.class);
        assertTrue(
                response.getStatusCode().is2xxSuccessful(),
                () -> "POST " + path + " failed: " + response.getStatusCode() + " body=" + response.getBody()
        );
        return response.getBody();
    }

    private Map<String, Object> exchange(HttpMethod method, String path, String token, Map<String, Object> body) {
        ResponseEntity<Map> response = restTemplate.exchange(path, method, jsonEntity(token, body), Map.class);
        assertTrue(
                response.getStatusCode().is2xxSuccessful(),
                () -> method + " " + path + " failed: " + response.getStatusCode() + " body=" + response.getBody()
        );
        return response.getBody();
    }

    private HttpEntity<Map<String, Object>> jsonEntity(String token, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return new HttpEntity<>(body, headers);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> data(Map<String, Object> response) {
        assertNotNull(response);
        return (Map<String, Object>) response.get("data");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> dataList(Map<String, Object> response) {
        assertNotNull(response);
        return (List<Map<String, Object>>) response.get("data");
    }

    private String string(Map<String, Object> data, String key) {
        Object value = data.get(key);
        assertNotNull(value);
        return value.toString();
    }

    private Number number(Map<String, Object> data, String key) {
        Object value = data.get(key);
        assertTrue(value instanceof Number, key + " must be numeric");
        return (Number) value;
    }
}
