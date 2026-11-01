package com.wishdiary.domain.user;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidProfileAvatarException extends CustomException {

    public InvalidProfileAvatarException() {
        super("INVALID_PROFILE_AVATAR", "프로필 사진을 다시 선택해 주세요.", HttpStatus.BAD_REQUEST);
    }
}
