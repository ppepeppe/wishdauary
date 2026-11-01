package com.wishdiary.application.reaction;

import java.util.List;

public interface GetReactionFeedUseCase {

    List<ReactionFeedEventResponse> getRecentFeed(Long childUserId);
}
