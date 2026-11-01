package com.wishdiary.domain.user;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidUserNicknameException extends CustomException {

    public InvalidUserNicknameException() {
        super("INVALID_USER_NICKNAME", "닉네임은 1자 이상 20자 이하로 적어 주세요.", HttpStatus.BAD_REQUEST);
    }
}
