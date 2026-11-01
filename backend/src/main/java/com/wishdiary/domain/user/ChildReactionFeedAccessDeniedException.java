package com.wishdiary.domain.user;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ChildReactionFeedAccessDeniedException extends CustomException {

    public ChildReactionFeedAccessDeniedException(Long userId) {
        super(
                "CHILD_REACTION_FEED_ACCESS_DENIED",
                "최근 반응은 아이 계정에서만 볼 수 있어요.",
                HttpStatus.FORBIDDEN
        );
    }
}
