package com.wishdiary.application.sharing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record SharingGroupRequest(@NotBlank @Size(max = 30) String name,
                                  @NotNull Set<@NotNull @Positive Long> memberIds) { }
