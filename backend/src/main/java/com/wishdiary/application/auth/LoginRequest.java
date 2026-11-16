package com.wishdiary.application.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @JsonAlias("email") @NotBlank @Size(max = 255) String loginId,
        @NotBlank String password
) {
    public LoginRequest {
        loginId = loginId == null ? null : loginId.strip();
    }
}
