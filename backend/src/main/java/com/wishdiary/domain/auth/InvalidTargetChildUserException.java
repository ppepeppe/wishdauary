package com.wishdiary.domain.auth;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidTargetChildUserException extends CustomException {

    public InvalidTargetChildUserException(Long userId) {
        super("INVALID_TARGET_CHILD_USER", "선택한 사용자는 아이 계정이 아니에요.", HttpStatus.BAD_REQUEST);
    }
}
