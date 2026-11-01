package com.wishdiary.application.diary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.wishdiary.application.reaction.DiaryCommentRepository;
import com.wishdiary.application.reaction.HeartReactionRepository;
import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.group.FamilyConnectionPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.group.GroupMemberRole;
import com.wishdiary.domain.user.AppRole;
import com.wishdiary.domain.diary.Diary;
import com.wishdiary.domain.diary.DiaryEmotion;
import com.wishdiary.domain.diary.DiaryVisibilityScope;
import com.wishdiary.domain.user.FamilyDiaryAccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiaryQueryServiceTest {

    @Mock
    private LoadDiaryQueryPort loadDiaryQueryPort;

    @Mock
    private LoadUserPort loadUserPort;

    @Mock
    private FamilyConnectionPort familyConnections;

    @Mock
    private HeartReactionRepository heartReactionRepository;

    @Mock
    private DiaryCommentRepository diaryCommentRepository;

    private DiaryQueryService diaryQueryService;

    @BeforeEach
    void setUp() {
        diaryQueryService = new DiaryQueryService(
                loadDiaryQueryPort,
                diaryCommentRepository,
                heartReactionRepository,
                loadUserPort,
                familyConnections,
                new DiaryQueryResponseMapper(),
                new com.wishdiary.application.diary.DiaryAudiencePolicy(org.mockito.Mockito.mock(com.wishdiary.application.sharing.SharingGroupPort.class), familyConnections)
        );
    }

    @Test
    void getReturnsDiaryExistenceWhenDiaryExists() {
        Diary diary = Diary.reconstitute(
                11L,
                1L,
                "content",
                DiaryEmotion.TIRED,
                DiaryVisibilityScope.PRIVATE,
                LocalDate.of(2026, 4, 11),
                LocalDateTime.of(2026, 4, 11, 8, 0)
        );

        when(loadDiaryQueryPort.existsByAuthorIdAndWrittenDate(1L, LocalDate.of(2026, 4, 11)))
                .thenReturn(true);
        when(loadDiaryQueryPort.countByAuthorIdAndWrittenDate(1L, LocalDate.of(2026, 4, 11)))
                .thenReturn(1L);
        when(loadDiaryQueryPort.loadLatestByAuthorIdAndWrittenDate(1L, LocalDate.of(2026, 4, 11)))
                .thenReturn(Optional.of(diary));
        when(heartReactionRepository.countByDiaryIds(List.of(11L))).thenReturn(java.util.Map.of(11L, 2L));
        when(diaryCommentRepository.countByDiaryIds(List.of(11L))).thenReturn(java.util.Map.of(11L, 1L));

        TodayDiaryExistenceResponse response = diaryQueryService.get(1L, LocalDate.of(2026, 4, 11));

        assertTrue(response.exists());
        assertEquals(1L, response.count());
        assertEquals(11L, response.diaryId());
        assertEquals("tired", response.emotion());
        assertEquals(2L, response.heartCount());
        assertEquals(1L, response.commentCount());
    }

    @Test
    void getReturnsDiaryAbsenceWhenDiaryDoesNotExist() {
        when(loadDiaryQueryPort.existsByAuthorIdAndWrittenDate(1L, LocalDate.of(2026, 4, 11)))
                .thenReturn(false);

        TodayDiaryExistenceResponse response = diaryQueryService.get(1L, LocalDate.of(2026, 4, 11));

        assertFalse(response.exists());
        assertEquals(0L, response.count());
        assertNull(response.diaryId());
        assertNull(response.emotion());
        assertEquals(0L, response.heartCount());
        assertEquals(0L, response.commentCount());
    }

    @Test
    void getReturnsLatestDiaryWhenMultipleDiariesExistOnSameDate() {
        Diary latestDiary = Diary.reconstitute(
                12L,
                1L,
                "latest content",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.FAMILY,
                LocalDate.of(2026, 4, 12),
                LocalDateTime.of(2026, 4, 12, 10, 0)
        );

        when(loadDiaryQueryPort.existsByAuthorIdAndWrittenDate(1L, LocalDate.of(2026, 4, 12)))
                .thenReturn(true);
        when(loadDiaryQueryPort.countByAuthorIdAndWrittenDate(1L, LocalDate.of(2026, 4, 12)))
                .thenReturn(2L);
        when(loadDiaryQueryPort.loadLatestByAuthorIdAndWrittenDate(1L, LocalDate.of(2026, 4, 12)))
                .thenReturn(Optional.of(latestDiary));
        when(heartReactionRepository.countByDiaryIds(List.of(12L))).thenReturn(java.util.Map.of(12L, 4L));
        when(diaryCommentRepository.countByDiaryIds(List.of(12L))).thenReturn(java.util.Map.of(12L, 3L));

        TodayDiaryExistenceResponse response = diaryQueryService.get(1L, LocalDate.of(2026, 4, 12));

        assertTrue(response.exists());
        assertEquals(2L, response.count());
        assertEquals(12L, response.diaryId());
        assertEquals("happy", response.emotion());
        assertEquals(4L, response.heartCount());
        assertEquals(3L, response.commentCount());
    }

    @Test
    void getReturnsDiarySummariesInResponseShape() {
        Diary latestDiary = Diary.reconstitute(
                20L,
                1L,
                "This is a very long diary content for preview mapping test.",
                DiaryEmotion.EXCITED,
                DiaryVisibilityScope.FAMILY,
                LocalDate.of(2026, 4, 11),
                LocalDateTime.of(2026, 4, 11, 9, 0)
        );
        Diary olderDiary = Diary.reconstitute(
                10L,
                1L,
                "older diary",
                null,
                DiaryVisibilityScope.PRIVATE,
                LocalDate.of(2026, 4, 10),
                LocalDateTime.of(2026, 4, 10, 8, 0)
        );

        when(loadDiaryQueryPort.loadAllByAuthorId(1L)).thenReturn(List.of(latestDiary, olderDiary));
        when(heartReactionRepository.countByDiaryIds(List.of(20L, 10L))).thenReturn(java.util.Map.of(20L, 3L, 10L, 0L));
        when(diaryCommentRepository.countByDiaryIds(List.of(20L, 10L))).thenReturn(java.util.Map.of(20L, 2L, 10L, 1L));

        List<MyDiarySummaryResponse> responses = diaryQueryService.get(1L);

        assertEquals(2, responses.size());
        assertEquals(20L, responses.get(0).diaryId());
        assertEquals("excited", responses.get(0).emotion());
        assertEquals("family", responses.get(0).visibilityScope());
        assertEquals(3L, responses.get(0).heartCount());
        assertEquals(2L, responses.get(0).commentCount());
        assertEquals("This is a very long diary content for pr...", responses.get(0).contentPreview());
    }

    @Test
    void getFamilyFeedReturnsSameGroupFamilyVisibleDiariesOnly() {
        Diary familyDiary = Diary.reconstitute(
                30L,
                4L,
                "Family visible diary for feed preview mapping.",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.FAMILY,
                LocalDate.of(2026, 4, 11),
                LocalDateTime.of(2026, 4, 11, 10, 0)
        );

        when(loadUserPort.loadById(2L)).thenReturn(new LoadedUser(2L, null, "수진", AppRole.FAMILY, 12L, 100L, GroupMemberRole.FAMILY, null, "엄마"));
        when(familyConnections.findChildUserIds(2L)).thenReturn(List.of(1L, 4L));
        when(familyConnections.isConnected(4L, 2L)).thenReturn(true);
        when(loadDiaryQueryPort.loadFamilyVisibleByAuthorIds(List.of(1L, 4L))).thenReturn(List.of(familyDiary));
        when(heartReactionRepository.countByDiaryIds(List.of(30L))).thenReturn(java.util.Map.of(30L, 2L));
        when(loadUserPort.loadByIds(List.of(1L, 4L))).thenReturn(java.util.Map.of(
                1L, new LoadedUser(1L, null, "민준", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null),
                4L, new LoadedUser(4L, null, "서윤", AppRole.CHILD, 14L, 100L, GroupMemberRole.CHILD, null, null)
        ));

        List<FamilyDiaryFeedResponse> responses = diaryQueryService.getFamilyFeed(2L);

        assertEquals(1, responses.size());
        assertEquals(30L, responses.get(0).diaryId());
        assertEquals(4L, responses.get(0).authorId());
        assertEquals("서윤", responses.get(0).authorNickname());
        assertNull(responses.get(0).authorRelationLabel());
        assertEquals("happy", responses.get(0).emotion());
        assertEquals(2L, responses.get(0).heartCount());
        assertEquals("Family visible diary for feed preview ma...", responses.get(0).contentPreview());
    }

    @Test
    void getFamilyFeedFailsWhenRequesterIsNotFamilyRole() {
        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, "민준", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));

        assertThrows(FamilyDiaryAccessDeniedException.class, () -> diaryQueryService.getFamilyFeed(1L));
    }
}
