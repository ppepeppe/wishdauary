package com.wishdiary.domain.auth;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidAuthTokenException extends CustomException {

    public InvalidAuthTokenException() {
        super("INVALID_AUTH_TOKEN", "로그인 정보가 유효하지 않거나 만료됐어요. 다시 로그인해 주세요.", HttpStatus.UNAUTHORIZED);
    }
}
