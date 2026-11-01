package com.wishdiary.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.wishdiary.application.clover.CloverBalanceResponse;
import com.wishdiary.application.clover.GetCloverBalanceUseCase;
import com.wishdiary.application.diary.GetTodayDiaryExistenceUseCase;
import com.wishdiary.application.diary.TodayDiaryExistenceResponse;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HomeSummaryQueryServiceTest {

    @Mock
    private GetTodayDiaryExistenceUseCase getTodayDiaryExistenceUseCase;

    @Mock
    private GetCloverBalanceUseCase getCloverBalanceUseCase;

    @Mock
    private GetUserStreakUseCase getUserStreakUseCase;

    private HomeSummaryQueryService homeSummaryQueryService;

    @BeforeEach
    void setUp() {
        homeSummaryQueryService = new HomeSummaryQueryService(
                getTodayDiaryExistenceUseCase,
                getCloverBalanceUseCase,
                getUserStreakUseCase
        );
    }

    @Test
    void getSummaryAggregatesTodayDiaryCloverAndStreak() {
        LocalDate date = LocalDate.of(2026, 4, 15);

        when(getTodayDiaryExistenceUseCase.get(1L, date))
                .thenReturn(new TodayDiaryExistenceResponse(true, 2L, 10L, "happy", 3L, 1L));
        when(getCloverBalanceUseCase.get(1L))
                .thenReturn(new CloverBalanceResponse(1L, 8));
        when(getUserStreakUseCase.getStreak(1L))
                .thenReturn(new UserStreakResponse(4, 7));

        HomeSummaryResponse response = homeSummaryQueryService.getSummary(1L, date);

        assertTrue(response.hasTodayDiary());
        assertEquals(10L, response.todayDiaryId());
        assertEquals("happy", response.todayDiaryEmotion());
        assertEquals(2L, response.todayDiaryCount());
        assertEquals(3L, response.todayDiaryHeartCount());
        assertEquals(1L, response.todayDiaryCommentCount());
        assertEquals(8, response.cloverBalance());
        assertEquals(4, response.currentStreak());
        assertEquals(7, response.maxStreak());
    }
}
