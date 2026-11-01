package com.wishdiary.application.reaction;

import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.reaction.DiaryComment;
import com.wishdiary.domain.reaction.HeartReaction;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ReactionFeedResponseMapper {

    public List<ReactionFeedEventResponse> toResponses(
            List<HeartReaction> heartReactions,
            List<DiaryComment> comments,
            Map<Long, LoadedUser> actors
    ) {
        return java.util.stream.Stream.concat(
                        heartReactions.stream().map(reaction -> toHeartResponse(reaction, actors)),
                        comments.stream().map(comment -> toCommentResponse(comment, actors))
                )
                .toList();
    }

    private ReactionFeedEventResponse toHeartResponse(HeartReaction heartReaction, Map<Long, LoadedUser> actors) {
        LoadedUser actor = actors.get(heartReaction.getFamilyUserId());
        return new ReactionFeedEventResponse(
                "heart",
                heartReaction.getDiaryId(),
                heartReaction.getFamilyUserId(),
                actor == null ? null : actor.nickname(),
                actor == null ? null : actor.relationLabel(),
                null,
                heartReaction.getCreatedAt()
        );
    }

    private ReactionFeedEventResponse toCommentResponse(DiaryComment diaryComment, Map<Long, LoadedUser> actors) {
        LoadedUser actor = actors.get(diaryComment.getAuthorId());
        return new ReactionFeedEventResponse(
                "comment",
                diaryComment.getDiaryId(),
                diaryComment.getAuthorId(),
                actor == null ? null : actor.nickname(),
                actor == null ? null : actor.relationLabel(),
                diaryComment.getContent(),
                diaryComment.getCreatedAt()
        );
    }
}
