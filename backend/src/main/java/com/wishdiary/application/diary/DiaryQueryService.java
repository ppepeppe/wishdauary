package com.wishdiary.application.diary;

import com.wishdiary.application.reaction.DiaryCommentRepository;
import com.wishdiary.application.reaction.HeartReactionRepository;
import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.group.FamilyConnectionPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.diary.Diary;
import com.wishdiary.domain.reaction.HeartReaction;
import com.wishdiary.domain.user.FamilyDiaryAccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DiaryQueryService implements GetTodayDiaryExistenceUseCase, GetMyDiaryListUseCase, GetFamilyDiaryFeedUseCase {

    private final DiaryAudiencePolicy audiencePolicy;

    private final LoadDiaryQueryPort loadDiaryQueryPort;
    private final DiaryCommentRepository diaryCommentRepository;
    private final HeartReactionRepository heartReactionRepository;
    private final LoadUserPort loadUserPort;
    private final FamilyConnectionPort familyConnections;
    private final DiaryQueryResponseMapper diaryQueryResponseMapper;

    public DiaryQueryService(
            LoadDiaryQueryPort loadDiaryQueryPort,
            DiaryCommentRepository diaryCommentRepository,
            HeartReactionRepository heartReactionRepository,
            LoadUserPort loadUserPort,
            FamilyConnectionPort familyConnections,
            DiaryQueryResponseMapper diaryQueryResponseMapper,
            DiaryAudiencePolicy audiencePolicy
    ) {
        this.loadDiaryQueryPort = loadDiaryQueryPort;
        this.diaryCommentRepository = diaryCommentRepository;
        this.heartReactionRepository = heartReactionRepository;
        this.loadUserPort = loadUserPort;
        this.familyConnections = familyConnections;
        this.audiencePolicy = audiencePolicy;
        this.diaryQueryResponseMapper = diaryQueryResponseMapper;
    }

    @Override
    public TodayDiaryExistenceResponse get(Long authorId, LocalDate date) {
        if (!loadDiaryQueryPort.existsByAuthorIdAndWrittenDate(authorId, date)) {
            return diaryQueryResponseMapper.toNotFoundTodayExistenceResponse();
        }

        long count = loadDiaryQueryPort.countByAuthorIdAndWrittenDate(authorId, date);
        Diary latestDiary = loadDiaryQueryPort.loadLatestByAuthorIdAndWrittenDate(authorId, date)
                .orElseGet(() -> {
                    throw new IllegalStateException("Diary existence state is inconsistent for authorId=" + authorId + ", date=" + date);
                });

        List<Long> diaryIds = List.of(latestDiary.getId());
        Map<Long, Long> heartCounts = heartReactionRepository.countByDiaryIds(diaryIds);
        Map<Long, Long> commentCounts = diaryCommentRepository.countByDiaryIds(diaryIds);
        return diaryQueryResponseMapper.toTodayExistenceResponse(latestDiary, count, heartCounts, commentCounts);
    }

    @Override
    public List<MyDiarySummaryResponse> get(Long authorId) {
        List<Diary> diaries = loadDiaryQueryPort.loadAllByAuthorId(authorId);
        List<Long> diaryIds = diaries.stream()
                .map(Diary::getId)
                .toList();
        Map<Long, Long> heartCounts = heartReactionRepository.countByDiaryIds(diaryIds);
        Map<Long, Long> commentCounts = diaryCommentRepository.countByDiaryIds(diaryIds);
        return diaryQueryResponseMapper.toMyDiarySummaryResponses(diaries, heartCounts, commentCounts);
    }

    @Override
    public List<FamilyDiaryFeedResponse> getFamilyFeed(Long familyUserId) {
        LoadedUser familyUser = loadUserPort.loadById(familyUserId);
        validateFamilyViewer(familyUser);

        List<Long> childIdsInSameGroup = familyConnections.findChildUserIds(familyUserId);

        Map<Long, LoadedUser> authors = loadUserPort.loadByIds(childIdsInSameGroup);
        List<Diary> diaries = loadDiaryQueryPort.loadFamilyVisibleByAuthorIds(childIdsInSameGroup).stream()
                .filter(diary -> audiencePolicy.canRead(familyUser, diary, authors.get(diary.getAuthorId())))
                .toList();
        List<Long> diaryIds = diaries.stream()
                .map(Diary::getId)
                .toList();
        Map<Long, Long> heartCounts = heartReactionRepository.countByDiaryIds(diaryIds);
        Map<Long, Long> commentCounts = diaryCommentRepository.countByDiaryIds(diaryIds);
        Set<Long> heartedDiaryIds = heartReactionRepository.findAllByDiaryIds(diaryIds)
                .stream()
                .filter(reaction -> familyUser.id().equals(reaction.getFamilyUserId()))
                .map(HeartReaction::getDiaryId)
                .collect(Collectors.toSet());
        return diaryQueryResponseMapper.toFamilyDiaryFeedResponses(diaries, heartCounts, commentCounts, heartedDiaryIds, authors);
    }

    private void validateFamilyViewer(LoadedUser familyUser) {
        if (!familyUser.isFamily()) {
            throw new FamilyDiaryAccessDeniedException(familyUser.id());
        }
    }
}
