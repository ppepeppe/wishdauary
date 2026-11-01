package com.wishdiary.domain.sharing;

public class SharingGroupAccessDeniedException extends RuntimeException {
    public SharingGroupAccessDeniedException() { super("이 공개 그룹을 관리할 수 없어요."); }
}
