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

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class DiaryStreakRewardIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate database;
    private final LocalDate start = LocalDate.now().minusDays(30);

    @Test
    void milestonesPayOneTwoAndThreeWhileMultipleEntriesCountAsOneDay() throws Exception {
        JsonNode child = signup();
        for (int day = 0; day < 8; day++) {
            create(child, start.plusDays(day));
            create(child, start.plusDays(day));
        }
        assertBonus(child, "STREAK_3", 1, 1);
        assertBonus(child, "STREAK_5", 1, 2);
        assertBonus(child, "STREAK_7", 1, 3);
        assertEquals(22, balance(child)); // 16 entries + 1 + 2 + 3
        assertEquals(8, streak(child));
    }

    @Test
    void aGapStartsANewCycleWithItsOwnMilestoneRewards() throws Exception {
        JsonNode child = signup();
        writeDays(child, start, 3);
        writeDays(child, start.plusDays(4), 7);
        assertBonus(child, "STREAK_3", 2, 2);
        assertBonus(child, "STREAK_5", 1, 2);
        assertBonus(child, "STREAK_7", 1, 3);
        assertEquals(17, balance(child));
        assertEquals(7, streak(child));
    }

    @Test
    void backdatedEntriesCannotRewindOrRepeatAnAlreadyReachedMilestone() throws Exception {
        JsonNode child = signup();
        writeDays(child, start, 3);
        create(child, start.minusDays(1));
        create(child, start.plusDays(1));
        create(child, start.plusDays(2));
        assertEquals(3, streak(child));
        assertBonus(child, "STREAK_3", 1, 1);
        assertEquals(7, balance(child));
        String latest = database.queryForObject("select last_written_date from user_stats where user_id=?", String.class, id(child));
        assertEquals(start.plusDays(2).toString(), latest);
    }

    @Test
    void concurrentFifthDayEntriesAllSaveButIssueTheBonusOnlyOnce() throws Exception {
        JsonNode child = signup();
        writeDays(child, start, 4);
        CyclicBarrier barrier = new CyclicBarrier(8);
        try (var pool = Executors.newFixedThreadPool(8)) {
            var results = new ArrayList<java.util.concurrent.Future<Integer>>();
            for (int i = 0; i < 8; i++) results.add(pool.submit(() -> {
                barrier.await(10, TimeUnit.SECONDS);
                return createStatus(child, start.plusDays(4));
            }));
            for (var result : results) assertEquals(201, result.get(20, TimeUnit.SECONDS));
        }
        assertEquals(5, streak(child));
        assertBonus(child, "STREAK_3", 1, 1);
        assertBonus(child, "STREAK_5", 1, 2);
        assertEquals(15, balance(child));
    }

    @Test
    void previouslyIssuedBonusesKeepTheirOriginalAmounts() throws Exception {
        JsonNode child = signup();
        writeDays(child, start, 2);
        database.update("insert into clover_ledgers(user_id,amount,reason,ref_id,created_at) values(?,?,?,?,current_timestamp)",
                id(child), 2, "STREAK_3", start.plusDays(2).toEpochDay());
        create(child, start.plusDays(2));
        assertBonus(child, "STREAK_3", 1, 2);
        assertEquals(5, balance(child));
    }

    private void writeDays(JsonNode child, LocalDate date, int count) throws Exception {
        for (int i = 0; i < count; i++) create(child, date.plusDays(i));
    }
    private long id(JsonNode child) { return child.get("userId").asLong(); }
    private int createStatus(JsonNode child, LocalDate date) throws Exception {
        return mvc.perform(post("/api/v1/diaries").header("Authorization", "Bearer " + child.get("accessToken").asText())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                        "content", "연속 기록 정책 확인", "visibilityScope", "private", "writtenDate", date.toString()))))
                .andReturn().getResponse().getStatus();
    }
    private void create(JsonNode child, LocalDate date) throws Exception { assertEquals(201, createStatus(child, date)); }
    private JsonNode signup() throws Exception {
        var response = mvc.perform(post("/api/v1/auth/signup/child").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("loginId", UUID.randomUUID().toString(), "password", "password", "nickname", "연속 기록 확인"))))
                .andReturn().getResponse();
        assertEquals(201, response.getStatus());
        return json.readTree(response.getContentAsByteArray()).get("data");
    }
    private void assertBonus(JsonNode child, String reason, int count, int total) {
        assertEquals(count, database.queryForObject("select count(*) from clover_ledgers where user_id=? and reason=?", Integer.class, id(child), reason));
        assertEquals(total, database.queryForObject("select coalesce(sum(amount),0) from clover_ledgers where user_id=? and reason=?", Integer.class, id(child), reason));
    }
    private int balance(JsonNode child) throws Exception { return fetch(child, "/api/v1/clovers/balance").get("balance").asInt(); }
    private int streak(JsonNode child) throws Exception { return fetch(child, "/api/v1/users/streak").get("currentStreak").asInt(); }
    private JsonNode fetch(JsonNode child, String path) throws Exception {
        var response = mvc.perform(get(path).header("Authorization", "Bearer " + child.get("accessToken").asText())).andReturn().getResponse();
        assertEquals(200, response.getStatus(), response.getContentAsString());
        return json.readTree(response.getContentAsByteArray()).get("data");
    }
}
