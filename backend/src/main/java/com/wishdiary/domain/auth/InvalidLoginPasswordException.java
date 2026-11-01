package com.wishdiary.domain.auth;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidLoginPasswordException extends CustomException {

    public InvalidLoginPasswordException() {
        super("INVALID_LOGIN_PASSWORD", "비밀번호가 맞지 않아요.", HttpStatus.UNAUTHORIZED);
    }
}
