package com.wishdiary.application.auth;

import com.wishdiary.domain.group.FamilyRelation;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FamilySignupRequest(
        @JsonAlias("email") @NotBlank @Size(max = 255) @Pattern(regexp = "(?U)\\S+") String loginId,
        @NotBlank String password,
        @NotBlank String nickname,
        @NotNull FamilyRelation familyRelation,
        @NotBlank String relationLabel,
        // Retained for old clients; it no longer establishes a connection.
        String inviteCode
) {
    public FamilySignupRequest {
        loginId = loginId == null ? null : loginId.strip();
    }
}
