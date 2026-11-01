package com.wishdiary.domain.diary;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidDiaryContentException extends CustomException {

    public InvalidDiaryContentException() {
        super("INVALID_DIARY_CONTENT", "일기 내용을 입력해 주세요.", HttpStatus.BAD_REQUEST);
    }
}

