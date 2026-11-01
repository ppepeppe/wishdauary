package com.wishdiary.application.diary;

import com.wishdiary.application.clover.RecordCloverLedgerPort;
import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.application.user.UpdateUserStreakPort;
import com.wishdiary.domain.clover.CloverLedger;
import com.wishdiary.domain.clover.CloverReason;
import com.wishdiary.domain.diary.Diary;
import com.wishdiary.domain.diary.DiaryEmotion;
import com.wishdiary.domain.diary.DiaryVisibilityScope;
import com.wishdiary.domain.user.UserStreakStats;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateDiaryService implements CreateDiaryUseCase {

    private static final int DIARY_WRITE_CLOVER_AMOUNT = 1;
    private static final int STREAK_3_BONUS_AMOUNT = 2;
    private static final int STREAK_7_BONUS_AMOUNT = 5;

    private final DiaryAudiencePolicy audiencePolicy;

    private final DiaryRepository diaryRepository;
    private final RecordCloverLedgerPort recordCloverLedgerPort;
    private final LoadUserPort loadUserPort;
    private final UpdateUserStreakPort updateUserStreakPort;
    private final CreateDiaryValidator createDiaryValidator;
    private final CreateDiaryResponseMapper createDiaryResponseMapper;

    public CreateDiaryService(
            DiaryRepository diaryRepository,
            RecordCloverLedgerPort recordCloverLedgerPort,
            LoadUserPort loadUserPort,
            UpdateUserStreakPort updateUserStreakPort,
            CreateDiaryValidator createDiaryValidator,
            CreateDiaryResponseMapper createDiaryResponseMapper,
            DiaryAudiencePolicy audiencePolicy
    ) {
        this.diaryRepository = diaryRepository;
        this.recordCloverLedgerPort = recordCloverLedgerPort;
        this.loadUserPort = loadUserPort;
        this.audiencePolicy = audiencePolicy;
        this.updateUserStreakPort = updateUserStreakPort;
        this.createDiaryValidator = createDiaryValidator;
        this.createDiaryResponseMapper = createDiaryResponseMapper;
    }

    @Override
    @Transactional
    public CreateDiaryResponse create(Long authorId, CreateDiaryRequest request) {
        LoadedUser author = loadUserPort.loadById(authorId);
        createDiaryValidator.validateAuthorCanCreate(author);

        Diary diary = createDiary(author, request);
        audiencePolicy.validateSelection(diary);
        Diary savedDiary = diaryRepository.save(diary);
        recordDiaryWriteClover(savedDiary);
        UserStreakStats updatedUserStreakStats = updateDiaryStreak(savedDiary);
        recordStreakBonus(savedDiary, updatedUserStreakStats);
        return createDiaryResponseMapper.map(savedDiary);
    }

    private Diary createDiary(LoadedUser author, CreateDiaryRequest request) {
        return Diary.create(
                author.id(),
                request.content(),
                DiaryEmotion.from(request.emotion()),
                DiaryVisibilityScope.from(request.visibilityScope()),
                request.writtenDate(),
                request.sharingGroupIds()
        );
    }

    private void recordDiaryWriteClover(Diary savedDiary) {
        CloverLedger cloverLedger = CloverLedger.create(
                savedDiary.getAuthorId(),
                DIARY_WRITE_CLOVER_AMOUNT,
                CloverReason.DIARY_WRITE,
                savedDiary.getId()
        );
        recordCloverLedgerPort.record(cloverLedger);
    }

    private UserStreakStats updateDiaryStreak(Diary savedDiary) {
        UserStreakStats userStreakStats = updateUserStreakPort.loadUserStreak(savedDiary.getAuthorId())
                .orElse(UserStreakStats.initialize(savedDiary.getAuthorId(), savedDiary.getWrittenDate()))
                .updateForWrittenDate(savedDiary.getWrittenDate());
        return updateUserStreakPort.saveUserStreak(userStreakStats);
    }

    private void recordStreakBonus(Diary savedDiary, UserStreakStats updatedUserStreakStats) {
        if (updatedUserStreakStats.currentStreak() == 3) {
            recordStreakBonusIfNeeded(savedDiary, CloverReason.STREAK_3, STREAK_3_BONUS_AMOUNT);
        }
        if (updatedUserStreakStats.currentStreak() == 7) {
            recordStreakBonusIfNeeded(savedDiary, CloverReason.STREAK_7, STREAK_7_BONUS_AMOUNT);
        }
    }

    private void recordStreakBonusIfNeeded(Diary savedDiary, CloverReason reason, int amount) {
        Long streakRefId = savedDiary.getWrittenDate().toEpochDay();
        boolean alreadyRecorded = recordCloverLedgerPort.existsByUserIdAndReasonAndRefId(
                savedDiary.getAuthorId(),
                reason,
                streakRefId
        );
        if (alreadyRecorded) {
            return;
        }

        CloverLedger streakBonusLedger = CloverLedger.create(
                savedDiary.getAuthorId(),
                amount,
                reason,
                streakRefId
        );
        recordCloverLedgerPort.record(streakBonusLedger);
    }
}
