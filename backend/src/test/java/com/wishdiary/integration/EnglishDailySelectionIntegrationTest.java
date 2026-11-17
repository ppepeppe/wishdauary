package com.wishdiary.integration;

import static org.junit.jupiter.api.Assertions.*;
import com.wishdiary.application.english.GetDailyEnglishContentUseCase;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class EnglishDailySelectionIntegrationTest {
    @Autowired GetDailyEnglishContentUseCase daily;
    @Autowired org.springframework.jdbc.core.JdbcTemplate db;
    @Test
    void simultaneousFirstReadersShareExactlyOnePersistedDailySelection() throws Exception {
        LocalDate date = LocalDate.of(2040, 12, 3);
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(6)) {
            var reads = new ArrayList<Future<Long>>();
            for (int i = 0; i < 6; i++) reads.add(pool.submit(() -> { start.await(); return daily.get(date).id(); }));
            start.countDown(); Long id = reads.get(0).get(20, TimeUnit.SECONDS);
            for (var read : reads) assertEquals(id, read.get(20, TimeUnit.SECONDS));
            assertEquals(1, db.queryForObject("select count(*) from english_daily_contents where study_date = ?", Integer.class, date));
        } finally { db.update("delete from english_daily_contents where study_date = ?", date); }
    }
}
