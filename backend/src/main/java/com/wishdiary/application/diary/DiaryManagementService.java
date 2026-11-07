package com.wishdiary.application.diary;

import com.wishdiary.application.reaction.DiaryCommentRepository;
import com.wishdiary.application.reaction.HeartReactionRepository;
import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.diary.Diary;
import com.wishdiary.domain.diary.DiaryAccessDeniedException;
import com.wishdiary.domain.diary.DiaryEmotion;
import com.wishdiary.domain.diary.DiaryNotFoundException;
import com.wishdiary.domain.diary.DiaryVisibilityScope;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiaryManagementService implements GetDiaryDetailUseCase, UpdateDiaryUseCase, DeleteDiaryUseCase {

    private final DiaryAudiencePolicy audiencePolicy;

    private final LoadDiaryQueryPort loadDiaryQueryPort;
    private final DiaryRepository diaryRepository;
    private final HeartReactionRepository heartReactionRepository;
    private final DiaryCommentRepository diaryCommentRepository;
    private final LoadUserPort loadUserPort;
    private final DiaryDetailResponseMapper diaryDetailResponseMapper;

    public DiaryManagementService(
            LoadDiaryQueryPort loadDiaryQueryPort,
            DiaryRepository diaryRepository,
            HeartReactionRepository heartReactionRepository,
            DiaryCommentRepository diaryCommentRepository,
            LoadUserPort loadUserPort,
            DiaryDetailResponseMapper diaryDetailResponseMapper,
            DiaryAudiencePolicy audiencePolicy
    ) {
        this.loadDiaryQueryPort = loadDiaryQueryPort;
        this.diaryRepository = diaryRepository;
        this.heartReactionRepository = heartReactionRepository;
        this.diaryCommentRepository = diaryCommentRepository;
        this.loadUserPort = loadUserPort;
        this.audiencePolicy = audiencePolicy;
        this.diaryDetailResponseMapper = diaryDetailResponseMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public DiaryDetailResponse getDetail(Long viewerUserId, Long diaryId) {
        LoadedUser viewer = loadUserPort.loadById(viewerUserId);
        Diary diary = loadDiary(diaryId);
        LoadedUser author = loadUserPort.loadById(diary.getAuthorId());
        validateReadAccess(viewer, diary, author);
        boolean hearted = viewer.isFamily()
                && heartReactionRepository.findByDiaryIdAndFamilyUserId(diaryId, viewerUserId).isPresent();
        return toDetailResponse(diary, hearted);
    }

    @Override
    @Transactional
    public DiaryDetailResponse update(Long requesterUserId, Long diaryId, UpdateDiaryRequest request) {
        LoadedUser requester = loadUserPort.loadById(requesterUserId);
        Diary diary = loadDiary(diaryId);
        validateOwnerAccess(requester, diary);

        Diary updatedDiary = diary.update(
                request.content(),
                DiaryEmotion.from(request.emotion()),
                DiaryVisibilityScope.from(request.visibilityScope()),
                request.sharingGroupIds()
        );
        audiencePolicy.validateSelection(updatedDiary);
        return toDetailResponse(diaryRepository.save(updatedDiary), false);
    }

    @Override
    @Transactional
    public void delete(Long requesterUserId, Long diaryId) {
        LoadedUser requester = loadUserPort.loadById(requesterUserId);
        Diary diary = loadDiary(diaryId);
        validateOwnerAccess(requester, diary);

        heartReactionRepository.deleteAllByDiaryId(diaryId);
        diaryCommentRepository.deleteAllByDiaryId(diaryId);
        diaryRepository.deleteById(diaryId);
    }

    private Diary loadDiary(Long diaryId) {
        return loadDiaryQueryPort.loadById(diaryId)
                .orElseThrow(() -> new DiaryNotFoundException(diaryId));
    }

    private void validateReadAccess(LoadedUser viewer, Diary diary, LoadedUser author) {
        if (audiencePolicy.canRead(viewer, diary, author)) return;
        throw new DiaryAccessDeniedException(diary.getId());
    }

    private void validateOwnerAccess(LoadedUser requester, Diary diary) {
        if (!requester.isChild() || !requester.id().equals(diary.getAuthorId())) {
            throw new DiaryAccessDeniedException(diary.getId());
        }
    }

    private DiaryDetailResponse toDetailResponse(Diary diary, boolean hearted) {
        List<Long> diaryIds = List.of(diary.getId());
        long heartCount = heartReactionRepository.countByDiaryIds(diaryIds).getOrDefault(diary.getId(), 0L);
        long commentCount = diaryCommentRepository.countByDiaryIds(diaryIds).getOrDefault(diary.getId(), 0L);
        return diaryDetailResponseMapper.toResponse(diary, heartCount, commentCount, hearted);
    }
}
