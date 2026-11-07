package com.wishdiary.application.diary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wishdiary.application.reaction.DiaryCommentRepository;
import com.wishdiary.application.reaction.HeartReactionRepository;
import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.diary.Diary;
import com.wishdiary.domain.diary.DiaryAccessDeniedException;
import com.wishdiary.domain.diary.DiaryEmotion;
import com.wishdiary.domain.diary.DiaryVisibilityScope;
import com.wishdiary.domain.group.GroupMemberRole;
import com.wishdiary.domain.user.AppRole;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiaryManagementServiceTest {

    @Mock
    private LoadDiaryQueryPort loadDiaryQueryPort;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private HeartReactionRepository heartReactionRepository;

    @Mock
    private DiaryCommentRepository diaryCommentRepository;

    @Mock
    private LoadUserPort loadUserPort;

    private DiaryManagementService diaryManagementService;

    @BeforeEach
    void setUp() {
        diaryManagementService = new DiaryManagementService(
                loadDiaryQueryPort,
                diaryRepository,
                heartReactionRepository,
                diaryCommentRepository,
                loadUserPort,
                new DiaryDetailResponseMapper(),
                new com.wishdiary.application.diary.DiaryAudiencePolicy(org.mockito.Mockito.mock(com.wishdiary.application.sharing.SharingGroupPort.class), org.mockito.Mockito.mock(com.wishdiary.application.group.FamilyConnectionPort.class))
        );
    }

    @Test
    void getDetailReturnsOwnerDiaryContent() {
        Diary diary = diary();
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(loadDiaryQueryPort.loadById(10L)).thenReturn(Optional.of(diary));
        when(heartReactionRepository.countByDiaryIds(java.util.List.of(10L))).thenReturn(Map.of(10L, 2L));
        when(diaryCommentRepository.countByDiaryIds(java.util.List.of(10L))).thenReturn(Map.of(10L, 3L));

        DiaryDetailResponse response = diaryManagementService.getDetail(1L, 10L);

        assertEquals("full diary", response.content());
        assertEquals(2L, response.heartCount());
        assertEquals(3L, response.commentCount());
        assertEquals(false, response.hearted());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void detailReflectsTheViewingFamilyMembersOwnHeart(boolean hearted) {
        LoadedUser family = new LoadedUser(3L, "family@test.com", "엄마", AppRole.FAMILY,
                13L, 100L, GroupMemberRole.FAMILY, null, "엄마");
        Diary publicDiary = diary().update("public diary", DiaryEmotion.HAPPY, DiaryVisibilityScope.PUBLIC);
        when(loadUserPort.loadById(3L)).thenReturn(family);
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(loadDiaryQueryPort.loadById(10L)).thenReturn(Optional.of(publicDiary));
        when(heartReactionRepository.findByDiaryIdAndFamilyUserId(10L, 3L)).thenReturn(hearted
                ? Optional.of(com.wishdiary.domain.reaction.HeartReaction.create(10L, 3L)) : Optional.empty());

        assertEquals(hearted, diaryManagementService.getDetail(3L, 10L).hearted());
    }

    @Test
    void updateFailsWhenRequesterIsNotDiaryOwner() {
        when(loadUserPort.loadById(2L)).thenReturn(otherChildUser());
        when(loadDiaryQueryPort.loadById(10L)).thenReturn(Optional.of(diary()));

        assertThrows(
                DiaryAccessDeniedException.class,
                () -> diaryManagementService.update(2L, 10L, new UpdateDiaryRequest("updated", "sad", "private"))
        );
    }

    @Test
    void deleteRemovesDiaryReactionsAndComments() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(loadDiaryQueryPort.loadById(10L)).thenReturn(Optional.of(diary()));

        diaryManagementService.delete(1L, 10L);

        verify(heartReactionRepository).deleteAllByDiaryId(10L);
        verify(diaryCommentRepository).deleteAllByDiaryId(10L);
        verify(diaryRepository).deleteById(10L);
    }

    @Test
    void updateSavesChangedDiaryContent() {
        Diary savedDiary = diary().update("updated diary", DiaryEmotion.SAD, DiaryVisibilityScope.FAMILY);
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(loadDiaryQueryPort.loadById(10L)).thenReturn(Optional.of(diary()));
        when(diaryRepository.save(any(Diary.class))).thenReturn(savedDiary);
        when(heartReactionRepository.countByDiaryIds(java.util.List.of(10L))).thenReturn(Map.of());
        when(diaryCommentRepository.countByDiaryIds(java.util.List.of(10L))).thenReturn(Map.of());

        DiaryDetailResponse response = diaryManagementService.update(
                1L,
                10L,
                new UpdateDiaryRequest("updated diary", "sad", "family")
        );

        assertEquals("updated diary", response.content());
        assertEquals("sad", response.emotion());
        assertEquals("family", response.visibilityScope());
    }

    private Diary diary() {
        return Diary.reconstitute(
                10L,
                1L,
                "full diary",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.PRIVATE,
                LocalDate.of(2026, 6, 22),
                LocalDateTime.of(2026, 6, 22, 9, 0)
        );
    }

    private LoadedUser childUser() {
        return new LoadedUser(1L, "child@test.com", "미아", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null);
    }

    private LoadedUser otherChildUser() {
        return new LoadedUser(2L, "other@test.com", "유나", AppRole.CHILD, 12L, 200L, GroupMemberRole.CHILD, null, null);
    }
}
