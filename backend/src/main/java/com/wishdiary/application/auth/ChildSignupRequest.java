package com.wishdiary.application.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChildSignupRequest(
        @JsonAlias("email") @NotBlank @Size(max = 255) @Pattern(regexp = "(?U)\\S+") String loginId,
        @NotBlank String password,
        @NotBlank @Size(max = 20) String nickname
) {
    public ChildSignupRequest {
        loginId = loginId == null ? null : loginId.strip();
        nickname = nickname == null ? null : nickname.strip();
    }
}
