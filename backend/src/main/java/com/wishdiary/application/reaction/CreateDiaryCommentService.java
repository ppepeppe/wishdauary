package com.wishdiary.application.reaction;

import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.diary.Diary;
import com.wishdiary.domain.diary.DiaryNotFoundException;
import com.wishdiary.domain.reaction.DiaryComment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateDiaryCommentService implements CreateDiaryCommentUseCase {

    private final DiaryCommentRepository diaryCommentRepository;
    private final ReactionTargetDiaryPort reactionTargetDiaryPort;
    private final LoadUserPort loadUserPort;
    private final ReactionAccessPolicy reactionAccessPolicy;
    private final DiaryCommentResponseMapper diaryCommentResponseMapper;

    public CreateDiaryCommentService(
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
    @Transactional
    public DiaryCommentResponse create(Long familyUserId, CreateDiaryCommentRequest request) {
        LoadedUser familyUser = loadUserPort.loadById(familyUserId);
        Diary diary = reactionTargetDiaryPort.loadById(request.diaryId())
                .orElseThrow(() -> new DiaryNotFoundException(request.diaryId()));
        LoadedUser diaryAuthor = loadUserPort.loadById(diary.getAuthorId());

        reactionAccessPolicy.validateFamilyWriteAccess(familyUser, diary, diaryAuthor);

        DiaryComment savedComment = diaryCommentRepository.save(
                DiaryComment.create(request.diaryId(), familyUserId, request.content())
        );
        return diaryCommentResponseMapper.toResponse(
                savedComment,
                java.util.Map.of(familyUser.id(), familyUser)
        );
    }
}
