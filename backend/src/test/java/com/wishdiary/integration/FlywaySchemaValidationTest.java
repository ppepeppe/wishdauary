package com.wishdiary.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FlywaySchemaValidationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Test
    void contextLoadsWithFlywaySchemaAndJpaValidation() {
        assertNotNull(dataSource);
        assertNotNull(flyway.info().current());
    }
}
