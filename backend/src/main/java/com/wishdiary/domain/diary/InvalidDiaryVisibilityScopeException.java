package com.wishdiary.domain.diary;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidDiaryVisibilityScopeException extends CustomException {

    public InvalidDiaryVisibilityScopeException() {
        super(
                "INVALID_DIARY_VISIBILITY_SCOPE",
                "공개 범위를 다시 선택해 주세요.",
                HttpStatus.BAD_REQUEST
        );
    }
}

