package com.wishdiary.domain.group;

public enum GroupMemberRole {
    CHILD,
    FAMILY;

    public boolean isChild() {
        return this == CHILD;
    }

    public boolean isFamily() {
        return this == FAMILY;
    }
}
