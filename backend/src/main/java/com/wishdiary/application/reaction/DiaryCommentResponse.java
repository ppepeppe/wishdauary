package com.wishdiary.application.reaction;

import java.time.LocalDateTime;

public record DiaryCommentResponse(
        Long commentId,
        Long diaryId,
        Long authorId,
        String actorNickname,
        String actorRelationLabel,
        String content,
        LocalDateTime createdAt
) {
}
