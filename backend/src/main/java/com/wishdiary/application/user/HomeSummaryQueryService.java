package com.wishdiary.application.user;

import com.wishdiary.application.clover.CloverBalanceResponse;
import com.wishdiary.application.clover.GetCloverBalanceUseCase;
import com.wishdiary.application.diary.GetTodayDiaryExistenceUseCase;
import com.wishdiary.application.diary.TodayDiaryExistenceResponse;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HomeSummaryQueryService implements GetHomeSummaryUseCase {

    private final GetTodayDiaryExistenceUseCase getTodayDiaryExistenceUseCase;
    private final GetCloverBalanceUseCase getCloverBalanceUseCase;
    private final GetUserStreakUseCase getUserStreakUseCase;

    public HomeSummaryQueryService(
            GetTodayDiaryExistenceUseCase getTodayDiaryExistenceUseCase,
            GetCloverBalanceUseCase getCloverBalanceUseCase,
            GetUserStreakUseCase getUserStreakUseCase
    ) {
        this.getTodayDiaryExistenceUseCase = getTodayDiaryExistenceUseCase;
        this.getCloverBalanceUseCase = getCloverBalanceUseCase;
        this.getUserStreakUseCase = getUserStreakUseCase;
    }

    @Override
    public HomeSummaryResponse getSummary(Long userId, LocalDate date) {
        TodayDiaryExistenceResponse todayDiary = getTodayDiaryExistenceUseCase.get(userId, date);
        CloverBalanceResponse cloverBalance = getCloverBalanceUseCase.get(userId);
        UserStreakResponse userStreak = getUserStreakUseCase.getStreak(userId);

        return new HomeSummaryResponse(
                todayDiary.exists(),
                todayDiary.diaryId(),
                todayDiary.emotion(),
                todayDiary.count(),
                todayDiary.heartCount(),
                todayDiary.commentCount(),
                cloverBalance.balance(),
                userStreak.currentStreak(),
                userStreak.maxStreak()
        );
    }
}
