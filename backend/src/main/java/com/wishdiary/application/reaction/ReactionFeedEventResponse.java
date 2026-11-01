package com.wishdiary.application.reaction;

import java.time.LocalDateTime;

public record ReactionFeedEventResponse(
        String type,
        Long diaryId,
        Long actorUserId,
        String actorNickname,
        String actorRelationLabel,
        String contentPreview,
        LocalDateTime createdAt
) {
}
