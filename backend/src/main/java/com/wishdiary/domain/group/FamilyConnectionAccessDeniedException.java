package com.wishdiary.domain.group;
public class FamilyConnectionAccessDeniedException extends RuntimeException {
    public FamilyConnectionAccessDeniedException() { super("이 가족 연결을 관리할 수 없어요."); }
}
