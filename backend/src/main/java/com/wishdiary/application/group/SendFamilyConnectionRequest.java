package com.wishdiary.application.group;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
public record SendFamilyConnectionRequest(@NotNull @Positive Long childUserId) { }
