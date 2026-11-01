package com.wishdiary.domain.diary;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class DiaryNotFoundException extends CustomException {

    public DiaryNotFoundException(Long diaryId) {
        super("DIARY_NOT_FOUND", "일기를 찾을 수 없어요.", HttpStatus.NOT_FOUND);
    }
}
