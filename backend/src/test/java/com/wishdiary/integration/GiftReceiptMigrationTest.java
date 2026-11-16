package com.wishdiary.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.sql.DriverManager;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class GiftReceiptMigrationTest {
    @Test
    void existingRequestsKeepTheirStatusAndHaveNoAssumedReceiptDate() throws Exception {
        String url = "jdbc:h2:mem:receipt-migration-" + UUID.randomUUID() + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url, "sa", "").target("17").load().migrate();
        try (var db = DriverManager.getConnection(url, "sa", ""); var sql = db.createStatement()) {
            for (String status : new String[]{"PENDING", "APPROVED", "REJECTED"}) {
                sql.executeUpdate("insert into gift_request_notifications (product_id,product_title,clover_price,requester_child_user_id,requester_child_nickname,receiver_family_user_id,message,status,created_at) values (1,'선물',2,100,'아이',200,'부탁', '" + status + "',current_timestamp)");
            }
            Flyway.configure().dataSource(url, "sa", "").load().migrate();
            try (var rows = sql.executeQuery("select count(*),count(received_at),sum(clover_price),count(distinct status) from gift_request_notifications")) {
                rows.next();
                assertEquals(3, rows.getInt(1));
                assertEquals(0, rows.getInt(2));
                assertEquals(6, rows.getInt(3));
                assertEquals(3, rows.getInt(4));
            }
        }
    }
}
