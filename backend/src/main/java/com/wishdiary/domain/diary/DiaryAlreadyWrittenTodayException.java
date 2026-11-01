package com.wishdiary.domain.diary;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class DiaryAlreadyWrittenTodayException extends CustomException {

    public DiaryAlreadyWrittenTodayException() {
        super("DIARY_ALREADY_WRITTEN_TODAY", "하루에 일기는 한 개만 쓸 수 있어요.", HttpStatus.CONFLICT);
    }
}

