package com.wishdiary.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
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
class CatalogManagementIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired org.springframework.jdbc.core.JdbcTemplate database;
    @Autowired jakarta.persistence.EntityManager entities;

    @Test
    void userRoleDefaultsIndependentlyOfChildOrFamilyAndCannotBeChangedThroughProfile() throws Exception {
        for (String appRole : new String[]{"child", "family"}) {
            JsonNode user = signup(appRole);
            long id = user.get("userId").asLong();
            assertEquals("USER", database.queryForObject("select user_role from users where id = ?", String.class, id));
            assertEquals("user", call(get("/api/v1/users/me"), user, null, 200).get("userRole").asText());
            database.update("update users set user_role = 'ADMIN' where id = ?", id);
            entities.clear();
            assertEquals("admin", call(get("/api/v1/users/me"), user, null, 200).get("userRole").asText());
            JsonNode saved = call(patch("/api/v1/users/me"), user,
                    Map.of("nickname", "관리자", "avatarPreset", "mint", "userRole", "USER"), 200);
            assertEquals("admin", saved.get("userRole").asText());
            entities.flush(); entities.clear();
            assertEquals("ADMIN", database.queryForObject("select user_role from users where id = ?", String.class, id));
        }
    }

    @Test
    void registeredCatalogProductsAreSharedWithEveryChildAndPersistTheirPhoto() throws Exception {
        JsonNode family = signup("family"), child = signup("child"), sibling = signup("child");
        database.update("update users set user_role = 'ADMIN' where id = ?", family.get("userId").asLong());
        entities.clear();
        JsonNode product = call(post("/api/v1/admin/shop/products"), family, productBody(), 201);
        long productId = product.get("productId").asLong();
        assertEquals("preset", product.get("source").asText());
        assertTrue(product.get("childUserId").isNull());
        assertTrue(product.get("receiverFamilyUserId").isNull());
        assertEquals("공통 선물", product.get("title").asText());
        assertTrue(product.get("imageUrl").asText().startsWith("data:image/jpeg;base64,"));
        entities.flush(); entities.clear();
        assertTrue(database.queryForObject("select octet_length(image_data) from shop_products where id = ?", Integer.class, productId) > 0);
        for (JsonNode viewer : new JsonNode[]{child, sibling}) {
            assertTrue(contains(call(get("/api/v1/shop/products"), viewer, null, 200), productId));
        }
        database.update("insert into shop_products (child_user_id,title,description,clover_price,source,created_at) values (?, '개인 선물','개인 상품',1,'CUSTOM',current_timestamp)", child.get("userId").asLong());
        JsonNode catalog = call(get("/api/v1/admin/shop/products"), family, null, 200);
        assertTrue(contains(catalog, productId));
        for (JsonNode item : catalog) assertEquals("preset", item.get("source").asText());
    }

    @Test
    void roleAuthorizationIsDeferredWhileExistingLoginIsStillRequired() throws Exception {
        JsonNode regularChild = signup("child");
        assertEquals("user", call(get("/api/v1/users/me"), regularChild, null, 200).get("userRole").asText());
        call(post("/api/v1/admin/shop/products"), regularChild, productBody(), 201);
        call(get("/api/v1/admin/shop/products"), regularChild, null, 200);
        call(post("/api/v1/admin/shop/products"), null, productBody(), 401);
        call(get("/api/v1/admin/shop/products"), null, null, 401);
    }

    @Test
    void invalidCatalogProductsAreRejectedWithoutSaving() throws Exception {
        JsonNode user = signup("family");
        int before = database.queryForObject("select count(*) from shop_products", Integer.class);
        for (var invalid : Map.of("title", "  ", "description", "  ", "cloverPrice", 0,
                "purchasePriceWon", -1, "productUrl", "javascript:alert(1)",
                "imageData", "data:image/png;base64,aGVsbG8=").entrySet()) {
            var body = new HashMap<String, Object>(productBody());
            body.put(invalid.getKey(), invalid.getValue());
            call(post("/api/v1/admin/shop/products"), user, body, 400);
        }
        assertEquals(before, database.queryForObject("select count(*) from shop_products", Integer.class));
    }

    private boolean contains(JsonNode products, long id) {
        for (JsonNode product : products) if (product.get("productId").asLong() == id) return true;
        return false;
    }

    private Map<String, Object> productBody() throws Exception {
        var image = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB), "png", image);
        return Map.of("title", " 공통 선물 ", "description", "모든 아이가 고를 수 있어요.",
                "cloverPrice", 5, "purchasePriceWon", 12000, "productUrl", "https://example.com/gift",
                "imageData", "data:image/png;base64," + Base64.getEncoder().encodeToString(image.toByteArray()));
    }

    private JsonNode signup(String role) throws Exception {
        var body = new HashMap<String, Object>(Map.of("email", UUID.randomUUID() + "@test.com", "password", "password",
                "nickname", "하루", "userRole", "ADMIN"));
        if (role.equals("family")) { body.put("familyRelation", "MOTHER"); body.put("relationLabel", "엄마"); }
        return call(post("/api/v1/auth/signup/" + role), null, body, 201);
    }

    private JsonNode call(MockHttpServletRequestBuilder request, JsonNode user, Object body, int expected) throws Exception {
        if (user != null) request.header("Authorization", "Bearer " + user.get("accessToken").asText());
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        var response = mvc.perform(request).andReturn().getResponse();
        assertEquals(expected, response.getStatus(), response.getContentAsString());
        return json.readTree(response.getContentAsByteArray()).get("data");
    }
}
