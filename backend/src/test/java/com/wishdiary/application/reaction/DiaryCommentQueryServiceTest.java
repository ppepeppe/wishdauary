package com.wishdiary.application.reaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.group.GroupMemberRole;
import com.wishdiary.domain.user.AppRole;
import com.wishdiary.domain.diary.Diary;
import com.wishdiary.domain.diary.DiaryEmotion;
import com.wishdiary.domain.diary.DiaryNotFoundException;
import com.wishdiary.domain.diary.DiaryVisibilityScope;
import com.wishdiary.domain.reaction.DiaryComment;
import com.wishdiary.domain.reaction.DiaryCommentReadNotAllowedException;
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
class DiaryCommentQueryServiceTest {

    @Mock
    private DiaryCommentRepository diaryCommentRepository;

    @Mock
    private ReactionTargetDiaryPort reactionTargetDiaryPort;

    @Mock
    private LoadUserPort loadUserPort;

    @Mock
    private com.wishdiary.application.group.FamilyConnectionPort familyConnections;

    private DiaryCommentQueryService diaryCommentQueryService;

    @BeforeEach
    void setUp() {
        diaryCommentQueryService = new DiaryCommentQueryService(
                diaryCommentRepository,
                reactionTargetDiaryPort,
                loadUserPort,
                new ReactionAccessPolicy(new com.wishdiary.application.diary.DiaryAudiencePolicy(org.mockito.Mockito.mock(com.wishdiary.application.sharing.SharingGroupPort.class), familyConnections)),
                new DiaryCommentResponseMapper()
        );
    }

    @Test
    void getCommentsReturnsCommentsForDiaryAuthorChild() {
        Diary diary = Diary.reconstitute(
                30L,
                1L,
                "family diary",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.FAMILY,
                LocalDate.of(2026, 4, 12),
                LocalDateTime.of(2026, 4, 12, 9, 0)
        );
        DiaryComment comment = DiaryComment.reconstitute(
                90L,
                30L,
                2L,
                "재밌었겠다",
                LocalDateTime.of(2026, 4, 12, 11, 0)
        );

        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, "민준", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));
        when(reactionTargetDiaryPort.loadById(30L)).thenReturn(Optional.of(diary));
        when(diaryCommentRepository.findAllByDiaryId(30L)).thenReturn(List.of(comment));
        when(loadUserPort.loadByIds(List.of(2L))).thenReturn(java.util.Map.of(
                2L, new LoadedUser(2L, null, "수진", AppRole.FAMILY, 12L, 100L, GroupMemberRole.FAMILY, null, "엄마")
        ));

        List<DiaryCommentResponse> responses = diaryCommentQueryService.getComments(30L, 1L);

        assertEquals(1, responses.size());
        assertEquals(90L, responses.get(0).commentId());
        assertEquals("수진", responses.get(0).actorNickname());
        assertEquals("엄마", responses.get(0).actorRelationLabel());
        assertEquals("재밌었겠다", responses.get(0).content());
    }

    @Test
    void getCommentsReturnsCommentsForConnectedFamily() {
        when(familyConnections.isConnected(1L, 2L)).thenReturn(true);
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
        when(diaryCommentRepository.findAllByDiaryId(30L)).thenReturn(List.of());
        when(loadUserPort.loadByIds(List.of())).thenReturn(java.util.Map.of());

        List<DiaryCommentResponse> responses = diaryCommentQueryService.getComments(30L, 2L);

        assertEquals(0, responses.size());
    }

    @Test
    void getCommentsFailsForUnconnectedFamily() {
        Diary diary = Diary.reconstitute(
                30L,
                1L,
                "family diary",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.FAMILY,
                LocalDate.of(2026, 4, 12),
                LocalDateTime.of(2026, 4, 12, 9, 0)
        );

        when(loadUserPort.loadById(5L)).thenReturn(new LoadedUser(5L, null, "현우", AppRole.FAMILY, 21L, 200L, GroupMemberRole.FAMILY, null, "아빠"));
        when(reactionTargetDiaryPort.loadById(30L)).thenReturn(Optional.of(diary));
        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, "민준", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));

        assertThrows(DiaryCommentReadNotAllowedException.class, () -> diaryCommentQueryService.getComments(30L, 5L));
    }

    @Test
    void getCommentsFailsForOtherChildInSameGroup() {
        Diary diary = Diary.reconstitute(
                30L,
                1L,
                "family diary",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.FAMILY,
                LocalDate.of(2026, 4, 12),
                LocalDateTime.of(2026, 4, 12, 9, 0)
        );

        when(loadUserPort.loadById(4L)).thenReturn(new LoadedUser(4L, null, "서윤", AppRole.CHILD, 14L, 100L, GroupMemberRole.CHILD, null, null));
        when(reactionTargetDiaryPort.loadById(30L)).thenReturn(Optional.of(diary));
        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, "민준", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));

        assertThrows(DiaryCommentReadNotAllowedException.class, () -> diaryCommentQueryService.getComments(30L, 4L));
    }

    @Test
    void authorCanReadExistingCommentsAfterMakingDiaryPrivate() {
        Diary diary = Diary.reconstitute(
                30L,
                1L,
                "private diary",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.PRIVATE,
                LocalDate.of(2026, 4, 12),
                LocalDateTime.of(2026, 4, 12, 9, 0)
        );

        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, "민준", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));
        when(reactionTargetDiaryPort.loadById(30L)).thenReturn(Optional.of(diary));

        assertEquals(java.util.List.of(), diaryCommentQueryService.getComments(30L, 1L));
    }

    @Test
    void getCommentsFailsWhenDiaryDoesNotExist() {
        when(loadUserPort.loadById(2L)).thenReturn(new LoadedUser(2L, null, "수진", AppRole.FAMILY, 12L, 100L, GroupMemberRole.FAMILY, null, "엄마"));
        when(reactionTargetDiaryPort.loadById(999L)).thenReturn(Optional.empty());

        assertThrows(DiaryNotFoundException.class, () -> diaryCommentQueryService.getComments(999L, 2L));
    }
}
