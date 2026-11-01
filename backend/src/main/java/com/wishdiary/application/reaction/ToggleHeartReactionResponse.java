package com.wishdiary.application.reaction;

public record ToggleHeartReactionResponse(
        Long diaryId,
        Long actorUserId,
        String actorNickname,
        String actorRelationLabel,
        boolean hearted,
        long heartCount
) {
}
