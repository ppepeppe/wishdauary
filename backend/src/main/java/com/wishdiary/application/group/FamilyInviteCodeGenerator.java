package com.wishdiary.application.group;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FamilyInviteCodeGenerator {

    public String generate() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }
}
