package com.wishdiary.domain.diary;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidDiaryEmotionException extends CustomException {

    public InvalidDiaryEmotionException() {
        super(
                "INVALID_DIARY_EMOTION",
                "기분을 다시 선택해 주세요.",
                HttpStatus.BAD_REQUEST
        );
    }
}
