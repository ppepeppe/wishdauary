package com.wishdiary;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

class WishDiaryBackendApplicationTests {

    @Test
    void applicationClassIsSpringBootApplication() {
        assertTrue(WishDiaryBackendApplication.class.isAnnotationPresent(SpringBootApplication.class));
    }
}
