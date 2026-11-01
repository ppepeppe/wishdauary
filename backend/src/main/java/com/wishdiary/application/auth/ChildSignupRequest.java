package com.wishdiary.application.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChildSignupRequest(
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotBlank String nickname
) {
}
