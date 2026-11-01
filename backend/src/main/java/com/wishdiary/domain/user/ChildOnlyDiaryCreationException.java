package com.wishdiary.domain.user;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ChildOnlyDiaryCreationException extends CustomException {

    public ChildOnlyDiaryCreationException() {
        super("CHILD_ONLY_DIARY_CREATION", "일기는 아이 계정에서만 작성할 수 있어요.", HttpStatus.FORBIDDEN);
    }
}

