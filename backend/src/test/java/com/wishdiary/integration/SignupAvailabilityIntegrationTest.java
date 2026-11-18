package com.wishdiary.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SignupAvailabilityIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void availabilityAndSignupPreventDuplicateIdsAndNicknamesAcrossRoles() throws Exception {
        String id = UUID.randomUUID().toString(), nickname = "검증" + id.substring(0, 12);
        assertAvailable("LOGIN_ID", id, true);
        assertAvailable("NICKNAME", nickname, true);
        var first = mvc.perform(post("/api/v1/auth/signup/child").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("loginId", id, "password", "password", "nickname", nickname)))).andReturn().getResponse();
        assertEquals(201, first.getStatus());
        assertAvailable("LOGIN_ID", " " + id + " ", false);
        assertAvailable("NICKNAME", nickname, false);
        var duplicate = mvc.perform(post("/api/v1/auth/signup/family").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("loginId", UUID.randomUUID().toString(), "password", "password", "nickname", nickname,
                        "familyRelation", "AUNT", "relationLabel", "이모")))).andReturn().getResponse();
        assertEquals(409, duplicate.getStatus());
        assertEquals("DUPLICATE_NICKNAME", json.readTree(duplicate.getContentAsByteArray()).get("code").asText());
        assertAvailable("LOGIN_ID", "two words", false);
        assertAvailable("NICKNAME", " ", false);
        assertAvailable("NICKNAME", "가".repeat(21), false);
    }

    private void assertAvailable(String field, String value, boolean expected) throws Exception {
        var response = mvc.perform(get("/api/v1/auth/signup/availability").param("field", field).param("value", value)).andReturn().getResponse();
        assertEquals(200, response.getStatus());
        assertEquals(expected, json.readTree(response.getContentAsByteArray()).get("data").get("available").asBoolean());
    }
}
