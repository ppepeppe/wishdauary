package com.wishdiary.domain.reaction;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class DiaryCommentWriteNotAllowedException extends CustomException {

    public DiaryCommentWriteNotAllowedException() {
        super(
                "DIARY_COMMENT_WRITE_NOT_ALLOWED",
                "이 일기에는 연결된 가족만 댓글을 남길 수 있어요.",
                HttpStatus.FORBIDDEN
        );
    }
}
