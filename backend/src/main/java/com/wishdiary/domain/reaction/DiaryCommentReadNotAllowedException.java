package com.wishdiary.domain.reaction;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class DiaryCommentReadNotAllowedException extends CustomException {

    public DiaryCommentReadNotAllowedException() {
        super(
                "DIARY_COMMENT_READ_NOT_ALLOWED",
                "이 일기의 댓글을 볼 권한이 없어요.",
                HttpStatus.FORBIDDEN
        );
    }
}
