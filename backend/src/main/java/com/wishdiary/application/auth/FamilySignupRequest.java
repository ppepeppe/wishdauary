package com.wishdiary.application.auth;

import com.wishdiary.domain.group.FamilyRelation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FamilySignupRequest(
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotBlank String nickname,
        @NotNull FamilyRelation familyRelation,
        @NotBlank String relationLabel,
        // Retained for old clients; it no longer establishes a connection.
        String inviteCode
) {
}
