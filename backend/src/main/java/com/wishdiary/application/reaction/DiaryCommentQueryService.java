package com.wishdiary.application.reaction;

import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.diary.Diary;
import com.wishdiary.domain.diary.DiaryNotFoundException;
import com.wishdiary.domain.reaction.DiaryComment;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DiaryCommentQueryService implements GetDiaryCommentsUseCase {

    private final DiaryCommentRepository diaryCommentRepository;
    private final ReactionTargetDiaryPort reactionTargetDiaryPort;
    private final LoadUserPort loadUserPort;
    private final ReactionAccessPolicy reactionAccessPolicy;
    private final DiaryCommentResponseMapper diaryCommentResponseMapper;

    public DiaryCommentQueryService(
            DiaryCommentRepository diaryCommentRepository,
            ReactionTargetDiaryPort reactionTargetDiaryPort,
            LoadUserPort loadUserPort,
            ReactionAccessPolicy reactionAccessPolicy,
            DiaryCommentResponseMapper diaryCommentResponseMapper
    ) {
        this.diaryCommentRepository = diaryCommentRepository;
        this.reactionTargetDiaryPort = reactionTargetDiaryPort;
        this.loadUserPort = loadUserPort;
        this.reactionAccessPolicy = reactionAccessPolicy;
        this.diaryCommentResponseMapper = diaryCommentResponseMapper;
    }

    @Override
    public List<DiaryCommentResponse> getComments(Long diaryId, Long viewerUserId) {
        LoadedUser viewer = loadUserPort.loadById(viewerUserId);
        Diary diary = reactionTargetDiaryPort.loadById(diaryId)
                .orElseThrow(() -> new DiaryNotFoundException(diaryId));
        LoadedUser diaryAuthor = loadUserPort.loadById(diary.getAuthorId());

        reactionAccessPolicy.validateCommentReadAccess(viewer, diary, diaryAuthor);

        List<DiaryComment> comments = diaryCommentRepository.findAllByDiaryId(diaryId);
        List<Long> actorIds = comments.stream()
                .map(DiaryComment::getAuthorId)
                .distinct()
                .toList();
        java.util.Map<Long, LoadedUser> actors = loadUserPort.loadByIds(actorIds);
        return diaryCommentResponseMapper.toResponses(comments, actors);
    }
}
