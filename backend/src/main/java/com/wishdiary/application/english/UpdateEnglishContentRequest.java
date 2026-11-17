package com.wishdiary.application.english;

import jakarta.validation.constraints.*;

public record UpdateEnglishContentRequest(@NotBlank @Size(max = 120) String sentence,
        @NotBlank @Size(max = 120) String meaning, @NotNull @Min(0) Long version) {}
