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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class DiaryPhotoIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate database;
    @Autowired jakarta.persistence.EntityManager entities;

    @Test
    void photosPersistAndSurviveOlderEditsAndCanBeReplacedRemovedAndDeleted() throws Exception {
        JsonNode child = signup("child");
        long id = create(child, photo(java.awt.Color.PINK));
        entities.flush(); entities.clear();
        String first = detail(child, id, 200).get("imageData").asText();
        byte[] stored = database.queryForObject("select image_data from diary_photos where diary_id=?", byte[].class, id);
        assertArrayEquals(java.util.Base64.getDecoder().decode(first.split(",")[1]), stored);
        assertEquals(24, javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(stored)).getWidth());
        call(patch("/api/v1/diaries/" + id), child, Map.of("content", "글만 수정", "visibilityScope", "private"), 200);
        assertEquals(first, detail(child, id, 200).get("imageData").asText());
        call(patch("/api/v1/diaries/" + id), child, Map.of("content", "사진 교체", "visibilityScope", "private", "imageData", photo(java.awt.Color.BLUE)), 200);
        assertNotEquals(first, detail(child, id, 200).get("imageData").asText());
        call(patch("/api/v1/diaries/" + id), child, Map.of("content", "사진 삭제", "visibilityScope", "private", "removePhoto", true), 200);
        assertTrue(detail(child, id, 200).get("imageData").isNull());
        call(patch("/api/v1/diaries/" + id), child, Map.of("content", "사진 다시 첨부", "visibilityScope", "private", "imageData", photo(java.awt.Color.PINK)), 200);
        call(delete("/api/v1/diaries/" + id), child, null, 200);
        entities.flush(); entities.clear();
        assertEquals(0, database.queryForObject("select count(*) from diary_photos where diary_id=?", Integer.class, id));
        detail(child, id, 404);
    }

    @Test
    void photoAccessFollowsTheDiaryAudienceAndOnlyTheChildOwnerCanEditIt() throws Exception {
        JsonNode child = signup("child"), family = signup("family"), outsider = signup("child");
        JsonNode connection = call(post("/api/v1/family-connections/requests"), family, Map.of("childUserId", child.get("userId").asLong()), 201);
        call(patch("/api/v1/family-connections/requests/" + connection.get("requestId").asLong()), child, Map.of("action", "APPROVE"), 200);
        long id = create(child, photo(java.awt.Color.PINK));
        String image = detail(child, id, 200).get("imageData").asText();
        detail(null, id, 401); detail(family, id, 403); detail(outsider, id, 403);
        call(patch("/api/v1/diaries/" + id), child, Map.of("content", "가족 공개", "visibilityScope", "family"), 200);
        assertEquals(image, detail(family, id, 200).get("imageData").asText());
        detail(outsider, id, 403);
        call(patch("/api/v1/diaries/" + id), family, Map.of("content", "수정 불가", "visibilityScope", "family", "removePhoto", true), 403);
        call(patch("/api/v1/diaries/" + id), child, Map.of("content", "나만 보기", "visibilityScope", "private"), 200);
        detail(family, id, 403);
        assertEquals(image, detail(child, id, 200).get("imageData").asText());
    }

    @Test
    void invalidPhotosDoNotCreateDiariesEarnCloversOrPartiallyChangeContent() throws Exception {
        JsonNode child = signup("child");
        for (String invalid : new String[]{"data:image/jpeg;base64,aGVsbG8=", "data:image/svg+xml;base64,PHN2Zy8+", "a".repeat(700001)}) {
            call(post("/api/v1/diaries"), child, Map.of("content", "잘못된 사진", "visibilityScope", "private", "writtenDate", LocalDate.now().toString(), "imageData", invalid), 400);
        }
        assertEquals(0, call(get("/api/v1/diaries"), child, null, 200).size());
        assertEquals(0, call(get("/api/v1/clovers/balance"), child, null, 200).get("balance").asInt());
        long id = create(child, photo(java.awt.Color.PINK));
        call(patch("/api/v1/diaries/" + id), child, Map.of("content", "바뀌면 안 됨", "visibilityScope", "public", "imageData", "invalid"), 400);
        JsonNode original = detail(child, id, 200);
        assertEquals("오늘의 이야기", original.get("content").asText());
        assertEquals("private", original.get("visibilityScope").asText());
        assertTrue(original.get("imageData").asText().startsWith("data:image/jpeg;base64,"));
        call(patch("/api/v1/diaries/" + id), child, Map.of("content", "충돌", "visibilityScope", "private", "removePhoto", true, "imageData", photo(java.awt.Color.BLUE)), 400);
        assertEquals(original.get("imageData"), detail(child, id, 200).get("imageData"));
        assertEquals(1, call(get("/api/v1/clovers/balance"), child, null, 200).get("balance").asInt());
    }

    private long create(JsonNode child, String photo) throws Exception {
        return call(post("/api/v1/diaries"), child, Map.of("content", "오늘의 이야기", "visibilityScope", "private", "writtenDate", LocalDate.now().toString(), "imageData", photo), 201).get("diaryId").asLong();
    }
    private JsonNode detail(JsonNode user, long id, int status) throws Exception { return call(get("/api/v1/diaries/" + id), user, null, status); }
    private JsonNode signup(String role) throws Exception {
        var body = new java.util.HashMap<String,Object>(Map.of("loginId", UUID.randomUUID().toString(), "password", "password", "nickname", TestNicknames.unique("사진 확인")));
        if (role.equals("family")) { body.put("familyRelation", "AUNT"); body.put("relationLabel", "고모"); }
        return call(post("/api/v1/auth/signup/" + role), null, body, 201);
    }
    private String photo(java.awt.Color color) throws Exception {
        var image = new java.awt.image.BufferedImage(24,16,java.awt.image.BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics(); graphics.setColor(color); graphics.fillRect(0,0,24,16); graphics.dispose();
        var bytes = new java.io.ByteArrayOutputStream(); javax.imageio.ImageIO.write(image,"png",bytes);
        return "data:image/png;base64,"+java.util.Base64.getEncoder().encodeToString(bytes.toByteArray());
    }
    private JsonNode call(MockHttpServletRequestBuilder request, JsonNode user, Object body, int expected) throws Exception {
        if (user != null) request.header("Authorization", "Bearer " + user.get("accessToken").asText());
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        var response = mvc.perform(request).andReturn().getResponse(); assertEquals(expected,response.getStatus(),response.getContentAsString());
        return json.readTree(response.getContentAsByteArray()).get("data");
    }
}
