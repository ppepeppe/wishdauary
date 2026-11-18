package com.wishdiary.domain.user;

public class DuplicateNicknameException extends RuntimeException {
    public DuplicateNicknameException() { super("이미 사용 중인 닉네임이에요."); }
}
