package com.wishdiary.application.reaction;

import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.reaction.DiaryComment;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DiaryCommentResponseMapper {

    public DiaryCommentResponse toResponse(DiaryComment diaryComment, Map<Long, LoadedUser> actors) {
        LoadedUser actor = actors.get(diaryComment.getAuthorId());
        return new DiaryCommentResponse(
                diaryComment.getId(),
                diaryComment.getDiaryId(),
                diaryComment.getAuthorId(),
                actor == null ? null : actor.nickname(),
                actor == null ? null : actor.relationLabel(),
                diaryComment.getContent(),
                diaryComment.getCreatedAt()
        );
    }

    public List<DiaryCommentResponse> toResponses(List<DiaryComment> comments, Map<Long, LoadedUser> actors) {
        return comments.stream()
                .map(comment -> toResponse(comment, actors))
                .toList();
    }
}
