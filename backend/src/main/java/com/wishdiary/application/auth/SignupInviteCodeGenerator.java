package com.wishdiary.application.auth;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SignupInviteCodeGenerator {

    public String generate() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }
}
