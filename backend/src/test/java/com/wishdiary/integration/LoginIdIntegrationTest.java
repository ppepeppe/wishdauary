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
class LoginIdIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired org.springframework.jdbc.core.JdbcTemplate database;

    @Test
    void childrenAndFamiliesCanSignUpAndLogInWithoutAnEmailAddress() throws Exception {
        for (String role : new String[]{"child", "family"}) {
            String loginId = "다유어리_" + UUID.randomUUID();
            JsonNode account = call(post("/api/v1/auth/signup/" + role),
                    signupBody("loginId", " " + loginId + " ", role), 201).get("data");
            JsonNode login = call(post("/api/v1/auth/login"),
                    Map.of("loginId", " " + loginId + " ", "password", "password"), 200).get("data");
            assertEquals(account.get("userId"), login.get("userId"));
            assertEquals(role, login.get("appRole").asText());
            JsonNode profile = call(get("/api/v1/users/me").header("Authorization",
                    "Bearer " + login.get("accessToken").asText()), null, 200).get("data");
            assertEquals(loginId, profile.get("loginId").asText());
            assertEquals(loginId, database.queryForObject("select email from users where id = ?",
                    String.class, account.get("userId").asLong()));
        }
    }

    @Test
    void duplicateIdsCannotBeRegisteredAcrossChildAndFamilyAccounts() throws Exception {
        String loginId = "same_" + UUID.randomUUID();
        call(post("/api/v1/auth/signup/child"), signupBody("loginId", loginId, "child"), 201);
        JsonNode duplicate = call(post("/api/v1/auth/signup/family"),
                signupBody("loginId", loginId, "family"), 409);
        assertEquals("DUPLICATE_LOGIN_ID", duplicate.get("code").asText());
        assertEquals("이미 사용 중인 아이디예요.", duplicate.get("message").asText());
    }

    @Test
    void legacyEmailAccountsAndOlderClientsKeepTheirExistingCredentials() throws Exception {
        String legacyEmail = UUID.randomUUID() + "+legacy@test.com";
        JsonNode account = call(post("/api/v1/auth/signup/child"),
                signupBody("email", legacyEmail, "child"), 201).get("data");
        for (String key : new String[]{"email", "loginId"}) {
            JsonNode login = call(post("/api/v1/auth/login"),
                    Map.of(key, legacyEmail, "password", "password"), 200).get("data");
            assertEquals(account.get("userId"), login.get("userId"));
        }
        JsonNode profile = call(get("/api/v1/users/me").header("Authorization",
                "Bearer " + account.get("accessToken").asText()), null, 200).get("data");
        assertEquals(legacyEmail, profile.get("loginId").asText());
        assertEquals(legacyEmail, profile.get("email").asText());
    }

    @Test
    void blankSpacedAndOverlongSignupIdsAreRejectedAndWrongPasswordsStillFail() throws Exception {
        for (String loginId : new String[]{"  ", "two words", "a".repeat(256)}) {
            for (String role : new String[]{"child", "family"}) {
                JsonNode error = call(post("/api/v1/auth/signup/" + role),
                        signupBody("loginId", loginId, role), 400);
                assertTrue(error.get("message").asText().startsWith("아이디:"));
            }
        }
        String loginId = "password_" + UUID.randomUUID();
        call(post("/api/v1/auth/signup/child"), signupBody("loginId", loginId, "child"), 201);
        call(post("/api/v1/auth/login"), Map.of("loginId", loginId, "password", "wrong"), 401);
    }

    @Test
    void signupTrimsNamesAndFamilyLabelsLikeProfileEditing() throws Exception {
        for (String role : new String[]{"child", "family"}) {
            var body = signupBody("loginId", UUID.randomUUID().toString(), role);
            body.put("nickname", " 하루 ");
            if (role.equals("family")) body.put("relationLabel", " 고모 ");
            JsonNode account = call(post("/api/v1/auth/signup/" + role), body, 201).get("data");
            assertEquals("하루", account.get("nickname").asText());
            if (role.equals("family")) assertEquals("고모", account.get("relationLabel").asText());
        }
    }

    @Test
    void overlongSignupNamesAndRelationshipsReturnValidationErrorsWithoutCreatingAccounts() throws Exception {
        for (String role : new String[]{"child", "family"}) {
            var body = signupBody("loginId", UUID.randomUUID().toString(), role);
            body.put("nickname", "가".repeat(21));
            JsonNode error = call(post("/api/v1/auth/signup/" + role), body, 400);
            assertTrue(error.get("message").asText().startsWith("닉네임:"));
            assertEquals(0, database.queryForObject("select count(*) from users where email = ?", Integer.class, body.get("loginId")));
        }
        var family = signupBody("loginId", UUID.randomUUID().toString(), "family");
        family.put("relationLabel", "가".repeat(101));
        JsonNode error = call(post("/api/v1/auth/signup/family"), family, 400);
        assertTrue(error.get("message").asText().startsWith("아이와의 관계:"));
        assertEquals(0, database.queryForObject("select count(*) from users where email = ?", Integer.class, family.get("loginId")));
    }

    private Map<String, Object> signupBody(String key, String loginId, String role) {
        var body = new HashMap<String, Object>(Map.of(key, loginId,
                "password", "password", "nickname", "가입 확인"));
        if (role.equals("family")) {
            body.put("familyRelation", "MOTHER"); body.put("relationLabel", "엄마");
        }
        return body;
    }

    private JsonNode call(MockHttpServletRequestBuilder request, Object body, int status) throws Exception {
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        var response = mvc.perform(request).andReturn().getResponse();
        assertEquals(status, response.getStatus());
        return json.readTree(response.getContentAsByteArray());
    }
}
