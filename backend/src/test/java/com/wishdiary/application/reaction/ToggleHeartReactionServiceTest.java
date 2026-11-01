package com.wishdiary.application.reaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.group.GroupMemberRole;
import com.wishdiary.domain.user.AppRole;
import com.wishdiary.domain.diary.Diary;
import com.wishdiary.domain.diary.DiaryEmotion;
import com.wishdiary.domain.diary.DiaryNotFoundException;
import com.wishdiary.domain.diary.DiaryVisibilityScope;
import com.wishdiary.domain.reaction.HeartReaction;
import com.wishdiary.domain.reaction.HeartReactionNotAllowedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ToggleHeartReactionServiceTest {

    @Mock
    private HeartReactionRepository heartReactionRepository;

    @Mock
    private ReactionTargetDiaryPort reactionTargetDiaryPort;

    @Mock
    private LoadUserPort loadUserPort;

    @Mock
    private com.wishdiary.application.group.FamilyConnectionPort familyConnections;

    private ToggleHeartReactionService toggleHeartReactionService;

    @BeforeEach
    void setUp() {
        toggleHeartReactionService = new ToggleHeartReactionService(
                heartReactionRepository,
                reactionTargetDiaryPort,
                loadUserPort,
                new com.wishdiary.application.diary.DiaryAudiencePolicy(org.mockito.Mockito.mock(com.wishdiary.application.sharing.SharingGroupPort.class), familyConnections)
        );
    }

    @Test
    void toggleCreatesHeartWhenNotExists() {
        when(familyConnections.isConnected(1L, 2L)).thenReturn(true);
        ToggleHeartReactionRequest request = new ToggleHeartReactionRequest(30L);
        Diary diary = Diary.reconstitute(
                30L,
                1L,
                "family diary",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.FAMILY,
                LocalDate.of(2026, 4, 12),
                LocalDateTime.of(2026, 4, 12, 9, 0)
        );

        when(loadUserPort.loadById(2L)).thenReturn(new LoadedUser(2L, null, "수진", AppRole.FAMILY, 12L, 100L, GroupMemberRole.FAMILY, null, "엄마"));
        when(reactionTargetDiaryPort.loadById(30L)).thenReturn(Optional.of(diary));
        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, "민준", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));
        when(heartReactionRepository.findByDiaryIdAndFamilyUserId(30L, 2L)).thenReturn(Optional.empty());
        when(heartReactionRepository.save(any(HeartReaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(heartReactionRepository.countByDiaryId(30L)).thenReturn(1L);

        ToggleHeartReactionResponse response = toggleHeartReactionService.toggle(2L, request);

        assertEquals(30L, response.diaryId());
        assertTrue(response.hearted());
        assertEquals(1L, response.heartCount());
        verify(heartReactionRepository).save(any(HeartReaction.class));
    }

    @Test
    void toggleDeletesHeartWhenExists() {
        when(familyConnections.isConnected(1L, 2L)).thenReturn(true);
        ToggleHeartReactionRequest request = new ToggleHeartReactionRequest(30L);
        Diary diary = Diary.reconstitute(
                30L,
                1L,
                "family diary",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.FAMILY,
                LocalDate.of(2026, 4, 12),
                LocalDateTime.of(2026, 4, 12, 9, 0)
        );
        HeartReaction existingReaction = HeartReaction.reconstitute(
                99L,
                30L,
                2L,
                LocalDateTime.of(2026, 4, 12, 9, 30)
        );

        when(loadUserPort.loadById(2L)).thenReturn(new LoadedUser(2L, null, "수진", AppRole.FAMILY, 12L, 100L, GroupMemberRole.FAMILY, null, "엄마"));
        when(reactionTargetDiaryPort.loadById(30L)).thenReturn(Optional.of(diary));
        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, "민준", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));
        when(heartReactionRepository.findByDiaryIdAndFamilyUserId(30L, 2L)).thenReturn(Optional.of(existingReaction));
        when(heartReactionRepository.countByDiaryId(30L)).thenReturn(0L);

        ToggleHeartReactionResponse response = toggleHeartReactionService.toggle(2L, request);

        assertFalse(response.hearted());
        assertEquals(0L, response.heartCount());
        verify(heartReactionRepository).deleteById(99L);
        verify(heartReactionRepository, never()).save(any(HeartReaction.class));
    }

    @Test
    void toggleFailsWhenDiaryIsPrivate() {
        ToggleHeartReactionRequest request = new ToggleHeartReactionRequest(30L);
        Diary diary = Diary.reconstitute(
                30L,
                1L,
                "private diary",
                DiaryEmotion.SAD,
                DiaryVisibilityScope.PRIVATE,
                LocalDate.of(2026, 4, 12),
                LocalDateTime.of(2026, 4, 12, 9, 0)
        );

        when(loadUserPort.loadById(2L)).thenReturn(new LoadedUser(2L, null, "수진", AppRole.FAMILY, 12L, 100L, GroupMemberRole.FAMILY, null, "엄마"));
        when(reactionTargetDiaryPort.loadById(30L)).thenReturn(Optional.of(diary));
        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, "민준", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));

        assertThrows(HeartReactionNotAllowedException.class, () -> toggleHeartReactionService.toggle(2L, request));
    }

    @Test
    void toggleFailsWhenFamilyIsNotConnected() {
        ToggleHeartReactionRequest request = new ToggleHeartReactionRequest(30L);
        Diary diary = Diary.reconstitute(
                30L,
                1L,
                "family diary",
                DiaryEmotion.EXCITED,
                DiaryVisibilityScope.FAMILY,
                LocalDate.of(2026, 4, 12),
                LocalDateTime.of(2026, 4, 12, 9, 0)
        );

        when(loadUserPort.loadById(5L)).thenReturn(new LoadedUser(5L, null, "현우", AppRole.FAMILY, 21L, 200L, GroupMemberRole.FAMILY, null, "아빠"));
        when(reactionTargetDiaryPort.loadById(30L)).thenReturn(Optional.of(diary));
        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, "민준", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));

        assertThrows(HeartReactionNotAllowedException.class, () -> toggleHeartReactionService.toggle(5L, request));
    }

    @Test
    void toggleFailsWhenRequesterIsChild() {
        ToggleHeartReactionRequest request = new ToggleHeartReactionRequest(30L);

        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, "민준", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));

        assertThrows(HeartReactionNotAllowedException.class, () -> toggleHeartReactionService.toggle(1L, request));
    }

    @Test
    void toggleFailsWhenDiaryDoesNotExist() {
        ToggleHeartReactionRequest request = new ToggleHeartReactionRequest(999L);

        when(loadUserPort.loadById(2L)).thenReturn(new LoadedUser(2L, null, "수진", AppRole.FAMILY, 12L, 100L, GroupMemberRole.FAMILY, null, "엄마"));
        when(reactionTargetDiaryPort.loadById(999L)).thenReturn(Optional.empty());

        assertThrows(DiaryNotFoundException.class, () -> toggleHeartReactionService.toggle(2L, request));
    }
}
