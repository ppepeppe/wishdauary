package com.wishdiary.domain.auth;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class MissingAuthTokenException extends CustomException {

    public MissingAuthTokenException() {
        super("MISSING_AUTH_TOKEN", "로그인이 필요해요.", HttpStatus.UNAUTHORIZED);
    }
}
