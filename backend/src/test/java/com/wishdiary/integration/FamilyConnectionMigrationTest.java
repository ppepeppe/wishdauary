package com.wishdiary.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class FamilyConnectionMigrationTest {
    @Test
    void migrationPreservesFamilyWithoutExpandingExistingDiaryAudiences() throws Exception {
        String url = "jdbc:h2:mem:migration-" + UUID.randomUUID() + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url, "sa", "").target("11").load().migrate();
        try (Connection db = DriverManager.getConnection(url, "sa", "")) {
            seedFamily(db);
            seedCustomGroups(db);
            Flyway.configure().dataSource(url, "sa", "").load().migrate();
            assertEquals(5, count(db, "select count(*) from diary_sharing_groups where owner_id = 100 and slot between 1 and 5"));
            assertEquals(1, count(db, "select count(*) from diary_sharing_groups where owner_id = 100 and slot = 0"));
            assertEquals(1, count(db, "select count(*) from diary_sharing_group_members m join diary_sharing_groups g on g.id=m.sharing_group_id where g.owner_id=100 and g.slot=0 and m.user_id=200"));
            assertEquals(0, count(db, "select count(*) from diary_sharing_group_members where sharing_group_id=10 and user_id=200"));
            assertEquals(1, count(db, "select count(*) from diary_audience_groups where diary_id=1000 and sharing_group_id=10"));
            assertEquals(0, count(db, "select count(*) from groups where invite_code_revoked=false"));
        }
    }

    private void seedFamily(Connection db) throws Exception {
        try (var sql = db.createStatement()) {
            sql.executeUpdate("insert into users (id,email,nickname,app_role,password_hash) values (100,'child@test.com','아이','CHILD','hash'), (200,'family@test.com','엄마','FAMILY','hash'), (300,'other@test.com','다른 가족','FAMILY','hash')");
            sql.executeUpdate("insert into groups (id,invite_code) values (100,'OLD-CODE')");
            sql.executeUpdate("insert into group_members (group_id,user_id,member_role) values (100,100,'CHILD'),(100,200,'FAMILY')");
        }
    }

    private void seedCustomGroups(Connection db) throws Exception {
        try (var sql = db.createStatement()) {
            for (int slot = 1; slot <= 5; slot++) {
                sql.executeUpdate("insert into diary_sharing_groups (id,owner_id,name,slot) values (" + (slot * 10) + ",100,'기존 그룹'," + slot + ")");
            }
            sql.executeUpdate("insert into diary_sharing_group_members (sharing_group_id,user_id) values (10,300)");
            sql.executeUpdate("insert into diaries (id,author_id,content,visibility_scope,written_date,created_at) values (1000,100,'선택한 그룹만','GROUP',current_date,current_timestamp)");
            sql.executeUpdate("insert into diary_audience_groups (diary_id,sharing_group_id) values (1000,10)");
        }
    }

    private long count(Connection db, String query) throws Exception {
        try (var sql = db.createStatement(); var result = sql.executeQuery(query)) {
            result.next();
            return result.getLong(1);
        }
    }
}
