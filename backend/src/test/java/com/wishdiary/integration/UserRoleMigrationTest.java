package com.wishdiary.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class UserRoleMigrationTest {
    @Test
    void existingAdultsBecomeAdminsWhileChildRolesAndCatalogProductsArePreserved() throws Exception {
        String url = "jdbc:h2:mem:roles-" + UUID.randomUUID() + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url, "sa", "").target("16").load().migrate();
        try (var db = DriverManager.getConnection(url, "sa", ""); var sql = db.createStatement()) {
            sql.executeUpdate("insert into users (email,nickname,app_role,password_hash) values ('legacy@test.com','기존 가족','FAMILY','hash')");
            sql.executeUpdate("insert into users (email,nickname,app_role,password_hash) values ('child@test.com','기존 아이','CHILD','hash')");
            Flyway.configure().dataSource(url, "sa", "").target("19").load().migrate();
            Flyway.configure().dataSource(url, "sa", "").load().migrate();
            try (var result = sql.executeQuery("select app_role,user_role,nickname from users where email='legacy@test.com'")) {
                assertTrue(result.next());
                assertEquals("FAMILY", result.getString("app_role"));
                assertEquals("ADMIN", result.getString("user_role"));
                assertEquals("기존 가족", result.getString("nickname"));
            }
            try (var child = sql.executeQuery("select user_role from users where email='child@test.com'")) {
                assertTrue(child.next());
                assertEquals("USER", child.getString(1));
            }
            try (var product = sql.executeQuery("select count(*) from shop_products where deleted = false and created_by_user_id is null")) {
                assertTrue(product.next());
                assertTrue(product.getInt(1) > 0);
            }
            assertThrows(SQLException.class, () -> sql.executeUpdate("update users set user_role='UNKNOWN'"));
        }
    }
}
