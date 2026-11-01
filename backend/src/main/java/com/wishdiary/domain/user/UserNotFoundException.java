package com.wishdiary.domain.user;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends CustomException {

    public UserNotFoundException(Long userId) {
        super("USER_NOT_FOUND", "사용자를 찾을 수 없어요.", HttpStatus.NOT_FOUND);
    }
}
