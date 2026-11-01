package com.wishdiary.domain.user;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class DuplicateUserEmailException extends CustomException {

    public DuplicateUserEmailException(String email) {
        super("DUPLICATE_USER_EMAIL", "이미 가입된 이메일이에요.", HttpStatus.CONFLICT);
    }
}
