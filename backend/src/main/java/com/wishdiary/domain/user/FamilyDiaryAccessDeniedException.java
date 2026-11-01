package com.wishdiary.domain.user;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class FamilyDiaryAccessDeniedException extends CustomException {

    public FamilyDiaryAccessDeniedException(Long userId) {
        super(
                "FAMILY_DIARY_ACCESS_DENIED",
                "가족 일기는 연결된 가족 계정에서만 볼 수 있어요.",
                HttpStatus.FORBIDDEN
        );
    }
}
