package com.wishdiary.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
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
class FamilyProfileRelationshipIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired org.springframework.jdbc.core.JdbcTemplate database;
    @Autowired jakarta.persistence.EntityManager entities;

    @Test
    void familyCanEditItsRelationshipAndConnectedChildrenSeeTheUpdatedLabel() throws Exception {
        JsonNode family = signup("family"), child = signup("child");
        JsonNode request = call(post("/api/v1/family-connections/requests"), family,
                Map.of("childUserId", child.get("userId").asLong()), 201);
        call(patch("/api/v1/family-connections/requests/" + request.get("requestId").asLong()),
                child, Map.of("action", "APPROVE"), 200);
        assertEquals("엄마", call(get("/api/v1/users/me"), family, null, 200).get("relationLabel").asText());
        JsonNode changed = call(patch("/api/v1/users/me"), family,
                Map.of("nickname", "새 이름", "avatarPreset", "mint", "avatarCharacter", "kuromi", "relationLabel", " 고모 "), 200);
        assertEquals("고모", changed.get("relationLabel").asText());
        assertEquals("kuromi", changed.get("avatarCharacter").asText());
        entities.flush(); entities.clear();
        assertEquals("고모", call(get("/api/v1/users/me"), family, null, 200).get("relationLabel").asText());
        assertEquals("고모", call(get("/api/v1/family-connections"), child, null, 200).get(0).get("relationLabel").asText());
        assertEquals("고모", call(get("/api/v1/shop/recipients"), child, null, 200).get(0).get("relationLabel").asText());
        assertEquals("AUNT", database.queryForObject("select family_relation from group_members where user_id = ?", String.class, family.get("userId").asLong()));
        JsonNode loggedIn = call(post("/api/v1/auth/login"), null,
                Map.of("loginId", changed.get("loginId").asText(), "password", "password"), 200);
        assertEquals("고모", loggedIn.get("relationLabel").asText());
    }

    @Test
    void olderProfileRequestsPreserveTheRelationshipAndCustomLabelsCanBeSaved() throws Exception {
        JsonNode family = signup("family");
        call(patch("/api/v1/users/me"), family, Map.of("nickname", "별이", "relationLabel", "큰고모"), 200);
        call(patch("/api/v1/users/me"), family, Map.of("nickname", "새 닉네임", "avatarPreset", "sky"), 200);
        entities.flush(); entities.clear();
        JsonNode profile = call(get("/api/v1/users/me"), family, null, 200);
        assertEquals("큰고모", profile.get("relationLabel").asText());
        assertEquals("OTHER_FAMILY", database.queryForObject("select family_relation from group_members where user_id = ?", String.class, family.get("userId").asLong()));
        assertEquals("user", profile.get("userRole").asText());
        assertEquals("family", profile.get("appRole").asText());
    }

    @Test
    void invalidLabelsDoNotPartiallySaveAndChildrenCannotSetARelationship() throws Exception {
        JsonNode family = signup("family"), child = signup("child");
        for (String label : new String[]{"  ", "가".repeat(101)}) {
            call(patch("/api/v1/users/me"), family, Map.of("nickname", "바뀌면 안 됨", "relationLabel", label), 400);
            JsonNode profile = call(get("/api/v1/users/me"), family, null, 200);
            assertEquals("하루", profile.get("nickname").asText());
            assertEquals("엄마", profile.get("relationLabel").asText());
        }
        call(patch("/api/v1/users/me"), child, Map.of("nickname", "하루", "relationLabel", "고모"), 400);
        assertTrue(call(get("/api/v1/users/me"), child, null, 200).get("relationLabel").isNull());
    }

    private JsonNode signup(String role) throws Exception {
        var body = new HashMap<String, Object>(Map.of("loginId", UUID.randomUUID().toString(), "password", "password", "nickname", "하루"));
        if (role.equals("family")) { body.put("familyRelation", "MOTHER"); body.put("relationLabel", "엄마"); }
        return call(post("/api/v1/auth/signup/" + role), null, body, 201);
    }

    private JsonNode call(MockHttpServletRequestBuilder request, JsonNode user, Object body, int expected) throws Exception {
        if (user != null) request.header("Authorization", "Bearer " + user.get("accessToken").asText());
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        var response = mvc.perform(request).andReturn().getResponse();
        assertEquals(expected, response.getStatus());
        return json.readTree(response.getContentAsByteArray()).get("data");
    }
}
