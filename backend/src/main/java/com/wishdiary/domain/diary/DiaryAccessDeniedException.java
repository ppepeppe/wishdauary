package com.wishdiary.domain.diary;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class DiaryAccessDeniedException extends CustomException {

    public DiaryAccessDeniedException(Long diaryId) {
        super("DIARY_ACCESS_DENIED", "이 일기를 볼 권한이 없어요.", HttpStatus.FORBIDDEN);
    }
}
