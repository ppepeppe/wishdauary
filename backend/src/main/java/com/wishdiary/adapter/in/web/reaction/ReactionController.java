package com.wishdiary.adapter.in.web.reaction;

import com.wishdiary.adapter.in.web.auth.Authenticated;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.application.reaction.CreateDiaryCommentRequest;
import com.wishdiary.application.reaction.CreateDiaryCommentUseCase;
import com.wishdiary.application.reaction.DiaryCommentResponse;
import com.wishdiary.application.reaction.GetDiaryCommentsUseCase;
import com.wishdiary.application.reaction.GetReactionFeedUseCase;
import com.wishdiary.application.reaction.ReactionFeedEventResponse;
import com.wishdiary.application.reaction.ToggleHeartReactionRequest;
import com.wishdiary.application.reaction.ToggleHeartReactionResponse;
import com.wishdiary.application.reaction.ToggleHeartReactionUseCase;
import com.wishdiary.common.response.CommonResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reactions")
public class ReactionController {

    private final ToggleHeartReactionUseCase toggleHeartReactionUseCase;
    private final CreateDiaryCommentUseCase createDiaryCommentUseCase;
    private final GetDiaryCommentsUseCase getDiaryCommentsUseCase;
    private final GetReactionFeedUseCase getReactionFeedUseCase;

    public ReactionController(
            ToggleHeartReactionUseCase toggleHeartReactionUseCase,
            CreateDiaryCommentUseCase createDiaryCommentUseCase,
            GetDiaryCommentsUseCase getDiaryCommentsUseCase,
            GetReactionFeedUseCase getReactionFeedUseCase
    ) {
        this.toggleHeartReactionUseCase = toggleHeartReactionUseCase;
        this.createDiaryCommentUseCase = createDiaryCommentUseCase;
        this.getDiaryCommentsUseCase = getDiaryCommentsUseCase;
        this.getReactionFeedUseCase = getReactionFeedUseCase;
    }

    @PostMapping("/hearts/toggle")
    public CommonResponse<ToggleHeartReactionResponse> toggleHeart(
            @Authenticated AuthenticatedSession session,
            @Valid @RequestBody ToggleHeartReactionRequest request
    ) {
        ToggleHeartReactionResponse response = toggleHeartReactionUseCase.toggle(session.userId(), request);
        return CommonResponse.success("Heart reaction toggled.", response);
    }

    @PostMapping("/comments")
    public CommonResponse<DiaryCommentResponse> createComment(
            @Authenticated AuthenticatedSession session,
            @Valid @RequestBody CreateDiaryCommentRequest request
    ) {
        DiaryCommentResponse response = createDiaryCommentUseCase.create(session.userId(), request);
        return CommonResponse.success("Diary comment created.", response);
    }

    @GetMapping("/comments")
    public CommonResponse<List<DiaryCommentResponse>> getComments(
            @RequestParam Long diaryId,
            @Authenticated AuthenticatedSession session
    ) {
        List<DiaryCommentResponse> response = getDiaryCommentsUseCase.getComments(diaryId, session.userId());
        return CommonResponse.success("Diary comments retrieved.", response);
    }

    @GetMapping("/feed")
    public CommonResponse<List<ReactionFeedEventResponse>> getReactionFeed(@Authenticated AuthenticatedSession session) {
        List<ReactionFeedEventResponse> response = getReactionFeedUseCase.getRecentFeed(session.userId());
        return CommonResponse.success("Reaction feed retrieved.", response);
    }
}
