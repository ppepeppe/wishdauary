package com.wishdiary.domain.user;

public class DuplicateLoginIdException extends RuntimeException {
    public DuplicateLoginIdException() {
        super("이미 사용 중인 아이디예요.");
    }
}
