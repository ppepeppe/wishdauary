package com.wishdiary.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MobileCorsIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void nativeAndWebClientsCanSendAuthenticatedApiRequests() throws Exception {
        for (String origin : new String[]{"capacitor://localhost", "https://localhost",
                "http://localhost:5187", "http://127.0.0.1:5187",
                "http://localhost:5173", "http://127.0.0.1:5173"}) {
            mockMvc.perform(options("/api/v1/auth/login")
                            .header("Origin", origin)
                            .header("Access-Control-Request-Method", "POST")
                            .header("Access-Control-Request-Headers", "authorization,content-type"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", origin));
        }
    }

    @Test
    void unrelatedOriginsRemainBlocked() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", "https://unrelated.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
