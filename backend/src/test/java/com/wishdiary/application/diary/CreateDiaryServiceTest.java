package com.wishdiary.application.diary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wishdiary.application.clover.RecordCloverLedgerPort;
import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.application.user.UpdateUserStreakPort;
import com.wishdiary.domain.group.GroupMemberRole;
import com.wishdiary.domain.user.AppRole;
import com.wishdiary.domain.clover.CloverLedger;
import com.wishdiary.domain.clover.CloverReason;
import com.wishdiary.domain.diary.Diary;
import com.wishdiary.domain.diary.DiaryEmotion;
import com.wishdiary.domain.diary.DiaryVisibilityScope;
import com.wishdiary.domain.user.ChildOnlyDiaryCreationException;
import com.wishdiary.domain.user.UserStreakStats;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateDiaryServiceTest {

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private RecordCloverLedgerPort recordCloverLedgerPort;

    @Mock
    private LoadUserPort loadUserPort;

    @Mock
    private UpdateUserStreakPort updateUserStreakPort;

    private CreateDiaryService createDiaryService;

    @BeforeEach
    void setUp() {
        CreateDiaryValidator createDiaryValidator = new CreateDiaryValidator();
        CreateDiaryResponseMapper createDiaryResponseMapper = new CreateDiaryResponseMapper();
        createDiaryService = new CreateDiaryService(
                diaryRepository,
                recordCloverLedgerPort,
                loadUserPort,
                updateUserStreakPort,
                createDiaryValidator,
                createDiaryResponseMapper,
                new DiaryAudiencePolicy(org.mockito.Mockito.mock(com.wishdiary.application.sharing.SharingGroupPort.class), org.mockito.Mockito.mock(com.wishdiary.application.group.FamilyConnectionPort.class))
        );
    }

    @Test
    void createFailsWhenAuthorIsNotChild() {
        CreateDiaryRequest request = new CreateDiaryRequest(
                "today diary",
                "happy",
                "private",
                LocalDate.of(2026, 4, 11)
        );

        when(loadUserPort.loadById(2L)).thenReturn(new LoadedUser(2L, null, null, AppRole.FAMILY, 12L, 100L, GroupMemberRole.FAMILY, null, "엄마"));

        assertThrows(ChildOnlyDiaryCreationException.class, () -> createDiaryService.create(2L, request));
    }

    @Test
    void createAllowsMultipleDiariesOnSameWrittenDate() {
        CreateDiaryRequest request = new CreateDiaryRequest(
                "today diary",
                "happy",
                "private",
                LocalDate.of(2026, 4, 11)
        );

        Diary firstSavedDiary = Diary.reconstitute(
                10L,
                1L,
                "today diary",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.PRIVATE,
                LocalDate.of(2026, 4, 11),
                java.time.LocalDateTime.of(2026, 4, 11, 9, 0)
        );
        Diary secondSavedDiary = Diary.reconstitute(
                11L,
                1L,
                "today diary",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.PRIVATE,
                LocalDate.of(2026, 4, 11),
                java.time.LocalDateTime.of(2026, 4, 11, 10, 0)
        );

        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, null, AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));
        when(diaryRepository.save(any(Diary.class))).thenReturn(firstSavedDiary, secondSavedDiary);
        when(recordCloverLedgerPort.record(any(CloverLedger.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(updateUserStreakPort.loadUserStreak(1L)).thenReturn(java.util.Optional.empty());
        when(updateUserStreakPort.saveUserStreak(any(UserStreakStats.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateDiaryResponse firstResponse = createDiaryService.create(1L, request);
        CreateDiaryResponse secondResponse = createDiaryService.create(1L, request);

        assertEquals(10L, firstResponse.diaryId());
        assertEquals(11L, secondResponse.diaryId());
        verify(diaryRepository, org.mockito.Mockito.times(2)).save(any(Diary.class));
    }

    @Test
    void createSavesDiaryAndReturnsResponse() {
        CreateDiaryRequest request = new CreateDiaryRequest(
                "today diary",
                "happy",
                "private",
                LocalDate.of(2026, 4, 11)
        );

        Diary savedDiary = Diary.reconstitute(
                10L,
                1L,
                "today diary",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.PRIVATE,
                LocalDate.of(2026, 4, 11),
                java.time.LocalDateTime.of(2026, 4, 11, 9, 0)
        );

        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, null, AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));
        when(diaryRepository.save(any(Diary.class))).thenReturn(savedDiary);
        when(recordCloverLedgerPort.record(any(CloverLedger.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(updateUserStreakPort.loadUserStreak(1L)).thenReturn(java.util.Optional.empty());
        when(updateUserStreakPort.saveUserStreak(any(UserStreakStats.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateDiaryResponse response = createDiaryService.create(1L, request);

        assertEquals(10L, response.diaryId());
        assertEquals(1L, response.authorId());
        assertEquals("happy", response.emotion());
        assertEquals("private", response.visibilityScope());
        verify(diaryRepository).save(any(Diary.class));
        verify(recordCloverLedgerPort).record(argThat(cloverLedger ->
                cloverLedger.getUserId().equals(1L)
                        && cloverLedger.getAmount() == 1
                        && cloverLedger.getReason() == CloverReason.DIARY_WRITE
                        && cloverLedger.getRefId().equals(10L)
        ));
        verify(updateUserStreakPort).saveUserStreak(argThat(userStreakStats ->
                userStreakStats.userId().equals(1L)
                        && userStreakStats.currentStreak() == 1
                        && userStreakStats.maxStreak() == 1
                        && userStreakStats.lastWrittenDate().equals(LocalDate.of(2026, 4, 11))
        ));
    }

    @Test
    void createIncreasesStreakWhenPreviousWrittenDateWasYesterday() {
        CreateDiaryRequest request = new CreateDiaryRequest(
                "today diary",
                "happy",
                "private",
                LocalDate.of(2026, 4, 11)
        );

        Diary savedDiary = Diary.reconstitute(
                10L,
                1L,
                "today diary",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.PRIVATE,
                LocalDate.of(2026, 4, 11),
                java.time.LocalDateTime.of(2026, 4, 11, 9, 0)
        );

        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, null, AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));
        when(diaryRepository.save(any(Diary.class))).thenReturn(savedDiary);
        when(recordCloverLedgerPort.record(any(CloverLedger.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(updateUserStreakPort.loadUserStreak(1L)).thenReturn(java.util.Optional.of(
                new UserStreakStats(1L, 2, 2, LocalDate.of(2026, 4, 10))
        ));
        when(updateUserStreakPort.saveUserStreak(any(UserStreakStats.class))).thenAnswer(invocation -> invocation.getArgument(0));

        createDiaryService.create(1L, request);

        verify(updateUserStreakPort).saveUserStreak(argThat(userStreakStats ->
                userStreakStats.userId().equals(1L)
                        && userStreakStats.currentStreak() == 3
                        && userStreakStats.maxStreak() == 3
                        && userStreakStats.lastWrittenDate().equals(LocalDate.of(2026, 4, 11))
        ));
        verify(recordCloverLedgerPort).record(argThat(cloverLedger ->
                cloverLedger.getUserId().equals(1L)
                        && cloverLedger.getAmount() == 2
                        && cloverLedger.getReason() == CloverReason.STREAK_3
                        && cloverLedger.getRefId().equals(LocalDate.of(2026, 4, 11).toEpochDay())
        ));
    }

    @Test
    void createKeepsStreakWhenDiaryIsWrittenAgainOnSameDate() {
        CreateDiaryRequest request = new CreateDiaryRequest(
                "second diary today",
                "happy",
                "private",
                LocalDate.of(2026, 4, 11)
        );

        Diary savedDiary = Diary.reconstitute(
                11L,
                1L,
                "second diary today",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.PRIVATE,
                LocalDate.of(2026, 4, 11),
                java.time.LocalDateTime.of(2026, 4, 11, 10, 0)
        );

        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, null, AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));
        when(diaryRepository.save(any(Diary.class))).thenReturn(savedDiary);
        when(recordCloverLedgerPort.record(any(CloverLedger.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(updateUserStreakPort.loadUserStreak(1L)).thenReturn(java.util.Optional.of(
                new UserStreakStats(1L, 3, 5, LocalDate.of(2026, 4, 11))
        ));
        when(updateUserStreakPort.saveUserStreak(any(UserStreakStats.class))).thenAnswer(invocation -> invocation.getArgument(0));

        createDiaryService.create(1L, request);

        verify(updateUserStreakPort).saveUserStreak(argThat(userStreakStats ->
                userStreakStats.userId().equals(1L)
                        && userStreakStats.currentStreak() == 3
                        && userStreakStats.maxStreak() == 5
                        && userStreakStats.lastWrittenDate().equals(LocalDate.of(2026, 4, 11))
        ));
        verify(recordCloverLedgerPort, org.mockito.Mockito.times(1)).record(argThat(cloverLedger ->
                cloverLedger.getReason() == CloverReason.DIARY_WRITE
        ));
    }

    @Test
    void createResetsStreakWhenWrittenDateIsNotContinuous() {
        CreateDiaryRequest request = new CreateDiaryRequest(
                "today diary",
                "happy",
                "private",
                LocalDate.of(2026, 4, 11)
        );

        Diary savedDiary = Diary.reconstitute(
                12L,
                1L,
                "today diary",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.PRIVATE,
                LocalDate.of(2026, 4, 11),
                java.time.LocalDateTime.of(2026, 4, 11, 9, 0)
        );

        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, null, AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));
        when(diaryRepository.save(any(Diary.class))).thenReturn(savedDiary);
        when(recordCloverLedgerPort.record(any(CloverLedger.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(updateUserStreakPort.loadUserStreak(1L)).thenReturn(java.util.Optional.of(
                new UserStreakStats(1L, 4, 6, LocalDate.of(2026, 4, 8))
        ));
        when(updateUserStreakPort.saveUserStreak(any(UserStreakStats.class))).thenAnswer(invocation -> invocation.getArgument(0));

        createDiaryService.create(1L, request);

        verify(updateUserStreakPort).saveUserStreak(argThat(userStreakStats ->
                userStreakStats.userId().equals(1L)
                        && userStreakStats.currentStreak() == 1
                        && userStreakStats.maxStreak() == 6
                        && userStreakStats.lastWrittenDate().equals(LocalDate.of(2026, 4, 11))
        ));
    }

    @Test
    void createAwardsSevenDayStreakBonus() {
        CreateDiaryRequest request = new CreateDiaryRequest(
                "today diary",
                "happy",
                "private",
                LocalDate.of(2026, 4, 11)
        );

        Diary savedDiary = Diary.reconstitute(
                13L,
                1L,
                "today diary",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.PRIVATE,
                LocalDate.of(2026, 4, 11),
                java.time.LocalDateTime.of(2026, 4, 11, 9, 0)
        );

        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, null, AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));
        when(diaryRepository.save(any(Diary.class))).thenReturn(savedDiary);
        when(recordCloverLedgerPort.record(any(CloverLedger.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(updateUserStreakPort.loadUserStreak(1L)).thenReturn(java.util.Optional.of(
                new UserStreakStats(1L, 6, 6, LocalDate.of(2026, 4, 10))
        ));
        when(updateUserStreakPort.saveUserStreak(any(UserStreakStats.class))).thenAnswer(invocation -> invocation.getArgument(0));

        createDiaryService.create(1L, request);

        verify(recordCloverLedgerPort).record(argThat(cloverLedger ->
                cloverLedger.getUserId().equals(1L)
                        && cloverLedger.getAmount() == 5
                        && cloverLedger.getReason() == CloverReason.STREAK_7
                        && cloverLedger.getRefId().equals(LocalDate.of(2026, 4, 11).toEpochDay())
        ));
    }

    @Test
    void createDoesNotAwardDuplicateStreakBonusOnSameDate() {
        CreateDiaryRequest request = new CreateDiaryRequest(
                "today diary",
                "happy",
                "private",
                LocalDate.of(2026, 4, 11)
        );

        Diary savedDiary = Diary.reconstitute(
                14L,
                1L,
                "today diary",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.PRIVATE,
                LocalDate.of(2026, 4, 11),
                java.time.LocalDateTime.of(2026, 4, 11, 9, 0)
        );

        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, null, AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));
        when(diaryRepository.save(any(Diary.class))).thenReturn(savedDiary);
        when(recordCloverLedgerPort.record(any(CloverLedger.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(updateUserStreakPort.loadUserStreak(1L)).thenReturn(java.util.Optional.of(
                new UserStreakStats(1L, 2, 2, LocalDate.of(2026, 4, 10))
        ));
        when(updateUserStreakPort.saveUserStreak(any(UserStreakStats.class))).thenReturn(
                new UserStreakStats(1L, 3, 3, LocalDate.of(2026, 4, 11))
        );
        when(recordCloverLedgerPort.existsByUserIdAndReasonAndRefId(
                1L,
                CloverReason.STREAK_3,
                LocalDate.of(2026, 4, 11).toEpochDay()
        )).thenReturn(true);

        createDiaryService.create(1L, request);

        verify(recordCloverLedgerPort, never()).record(argThat(cloverLedger ->
                cloverLedger.getReason() == CloverReason.STREAK_3 && cloverLedger.getAmount() == 2
        ));
    }
}
