package com.wishdiary.application.english;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEnglishContentRequest(@NotBlank @Size(max = 120) String sentence,
        @NotBlank @Size(max = 120) String meaning) {}
