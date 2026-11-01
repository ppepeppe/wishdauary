package com.wishdiary.application.group;
import jakarta.validation.constraints.NotNull;
public record ProcessFamilyConnectionRequest(@NotNull Action action) {
    public enum Action { APPROVE, REJECT }
}
