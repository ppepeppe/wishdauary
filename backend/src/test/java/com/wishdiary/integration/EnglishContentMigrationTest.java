package com.wishdiary.integration;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.DriverManager;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class EnglishContentMigrationTest {
    @Test
    void initialContentsMoveToTheDatabaseWithoutChangingExistingStudySnapshots() throws Exception {
        String url = "jdbc:h2:mem:english-migration-" + UUID.randomUUID() + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url, "sa", "").target("21").load().migrate();
        try (var db = DriverManager.getConnection(url, "sa", ""); var sql = db.createStatement()) {
            sql.executeUpdate("insert into english_studies (child_user_id,receiver_family_user_id,study_date,sentence,meaning,status,created_at) values (1,2,'2026-11-17','Original sentence','원래 뜻','PENDING',current_timestamp)");
            Flyway.configure().dataSource(url, "sa", "").load().migrate();
            try (var rows = sql.executeQuery("select count(*) from english_contents")) { assertTrue(rows.next()); assertEquals(30, rows.getInt(1)); }
            try (var rows = sql.executeQuery("select sentence,meaning from english_contents order by id")) {
                assertTrue(rows.next()); assertEquals("Hello!", rows.getString(1)); assertEquals("안녕!", rows.getString(2));
            }
            try (var rows = sql.executeQuery("select sentence,meaning,status from english_studies")) {
                assertTrue(rows.next()); assertEquals("Original sentence", rows.getString(1));
                assertEquals("원래 뜻", rows.getString(2)); assertEquals("PENDING", rows.getString(3));
            }
            Flyway.configure().dataSource(url, "sa", "").load().migrate();
            try (var rows = sql.executeQuery("select count(*) from english_contents")) { assertTrue(rows.next()); assertEquals(30, rows.getInt(1)); }
        }
    }
}
