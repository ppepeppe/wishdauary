package com.wishdiary.domain.auth;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class LoginUserNotFoundException extends CustomException {

    public LoginUserNotFoundException(String email) {
        super("LOGIN_USER_NOT_FOUND", "가입된 계정을 찾을 수 없어요. 이메일을 확인해 주세요.", HttpStatus.NOT_FOUND);
    }
}
