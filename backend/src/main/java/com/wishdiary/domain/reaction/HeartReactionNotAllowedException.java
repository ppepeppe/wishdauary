package com.wishdiary.domain.reaction;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class HeartReactionNotAllowedException extends CustomException {

    public HeartReactionNotAllowedException() {
        super(
                "HEART_REACTION_NOT_ALLOWED",
                "이 일기에는 연결된 가족만 하트를 남길 수 있어요.",
                HttpStatus.FORBIDDEN
        );
    }
}
