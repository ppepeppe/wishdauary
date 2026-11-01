package com.wishdiary.domain.user;

public enum AppRole {
    CHILD,
    FAMILY;

    public boolean isChild() {
        return this == CHILD;
    }

    public boolean isFamily() {
        return this == FAMILY;
    }
}
