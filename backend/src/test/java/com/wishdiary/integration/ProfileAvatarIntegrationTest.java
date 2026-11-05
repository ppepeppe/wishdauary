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
class ProfileAvatarIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired jakarta.persistence.EntityManager entities;
    @Autowired org.springframework.jdbc.core.JdbcTemplate database;

    @Test
    void eachRoleCanSaveItsCharacterAndColorWhileOlderRequestsPreserveTheCharacter() throws Exception {
        for (String role : new String[]{"child", "family"}) {
            JsonNode user = signup(role);
            assertEquals("my-melody", call(get("/api/v1/users/me"), user, null, 200).get("avatarCharacter").asText());
            for (String character : new String[]{"kuromi", "cinnamoroll", "my-melody"}) {
                call(patch("/api/v1/users/me"), user, Map.of("nickname", "하루", "avatarPreset", "lavender",
                        "avatarCharacter", character), 200);
                entities.flush(); entities.clear();
                JsonNode profile = call(get("/api/v1/users/me"), user, null, 200);
                assertEquals(character, profile.get("avatarCharacter").asText());
                assertEquals("lavender", profile.get("avatarPreset").asText());
                assertEquals(character, database.queryForObject("select avatar_character from users where id = ?",
                        String.class, user.get("userId").asLong()));
                call(patch("/api/v1/users/me"), user, Map.of("nickname", "별이", "avatarPreset", "sky"), 200);
                assertEquals(character, call(get("/api/v1/users/me"), user, null, 200).get("avatarCharacter").asText());
            }
            call(patch("/api/v1/users/me"), user, Map.of("nickname", "별이", "avatarPreset", "sky", "avatarCharacter", "unknown"), 400);
            assertEquals("my-melody", call(get("/api/v1/users/me"), user, null, 200).get("avatarCharacter").asText());
        }
    }

    private JsonNode signup(String role) throws Exception {
        var body = new HashMap<String, Object>(Map.of("email", UUID.randomUUID() + "@test.com", "password", "password", "nickname", "하루"));
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
