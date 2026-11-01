package com.wishdiary.application.reaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.group.GroupMemberRole;
import com.wishdiary.domain.user.AppRole;
import com.wishdiary.domain.diary.Diary;
import com.wishdiary.domain.diary.DiaryEmotion;
import com.wishdiary.domain.diary.DiaryNotFoundException;
import com.wishdiary.domain.diary.DiaryVisibilityScope;
import com.wishdiary.domain.diary.InvalidDiaryContentException;
import com.wishdiary.domain.reaction.DiaryComment;
import com.wishdiary.domain.reaction.DiaryCommentWriteNotAllowedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateDiaryCommentServiceTest {

    @Mock
    private DiaryCommentRepository diaryCommentRepository;

    @Mock
    private ReactionTargetDiaryPort reactionTargetDiaryPort;

    @Mock
    private LoadUserPort loadUserPort;

    @Mock
    private com.wishdiary.application.group.FamilyConnectionPort familyConnections;

    private CreateDiaryCommentService createDiaryCommentService;

    @BeforeEach
    void setUp() {
        createDiaryCommentService = new CreateDiaryCommentService(
                diaryCommentRepository,
                reactionTargetDiaryPort,
                loadUserPort,
                new ReactionAccessPolicy(new com.wishdiary.application.diary.DiaryAudiencePolicy(org.mockito.Mockito.mock(com.wishdiary.application.sharing.SharingGroupPort.class), familyConnections)),
                new DiaryCommentResponseMapper()
        );
    }

    @Test
    void createSavesCommentWhenFamilyUserCanAccessDiary() {
        when(familyConnections.isConnected(1L, 2L)).thenReturn(true);
        CreateDiaryCommentRequest request = new CreateDiaryCommentRequest(30L, "재밌었겠다");
        Diary diary = Diary.reconstitute(
                30L,
                1L,
                "family diary",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.FAMILY,
                LocalDate.of(2026, 4, 12),
                LocalDateTime.of(2026, 4, 12, 9, 0)
        );
        DiaryComment savedComment = DiaryComment.reconstitute(
                90L,
                30L,
                2L,
                "재밌었겠다",
                LocalDateTime.of(2026, 4, 12, 11, 0)
        );

        when(loadUserPort.loadById(2L)).thenReturn(new LoadedUser(2L, null, "수진", AppRole.FAMILY, 12L, 100L, GroupMemberRole.FAMILY, null, "엄마"));
        when(reactionTargetDiaryPort.loadById(30L)).thenReturn(Optional.of(diary));
        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, "민준", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));
        when(diaryCommentRepository.save(any(DiaryComment.class))).thenReturn(savedComment);

        DiaryCommentResponse response = createDiaryCommentService.create(2L, request);

        assertEquals(90L, response.commentId());
        assertEquals(30L, response.diaryId());
        assertEquals(2L, response.authorId());
        assertEquals("수진", response.actorNickname());
        assertEquals("엄마", response.actorRelationLabel());
        assertEquals("재밌었겠다", response.content());
    }

    @Test
    void createFailsWhenDiaryIsPrivate() {
        CreateDiaryCommentRequest request = new CreateDiaryCommentRequest(30L, "재밌었겠다");
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

        assertThrows(DiaryCommentWriteNotAllowedException.class, () -> createDiaryCommentService.create(2L, request));
    }

    @Test
    void createFailsWhenFamilyIsNotConnected() {
        CreateDiaryCommentRequest request = new CreateDiaryCommentRequest(30L, "재밌었겠다");
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

        assertThrows(DiaryCommentWriteNotAllowedException.class, () -> createDiaryCommentService.create(5L, request));
    }

    @Test
    void createFailsWhenRequesterIsChild() {
        CreateDiaryCommentRequest request = new CreateDiaryCommentRequest(30L, "재밌었겠다");

        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, "민준", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));
        when(reactionTargetDiaryPort.loadById(30L)).thenReturn(Optional.of(
                Diary.reconstitute(
                        30L,
                        1L,
                        "family diary",
                        DiaryEmotion.HAPPY,
                        DiaryVisibilityScope.FAMILY,
                        LocalDate.of(2026, 4, 12),
                        LocalDateTime.of(2026, 4, 12, 9, 0)
                )
        ));
        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, "민준", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));

        assertThrows(DiaryCommentWriteNotAllowedException.class, () -> createDiaryCommentService.create(1L, request));
    }

    @Test
    void createFailsWhenDiaryDoesNotExist() {
        CreateDiaryCommentRequest request = new CreateDiaryCommentRequest(999L, "재밌었겠다");

        when(loadUserPort.loadById(2L)).thenReturn(new LoadedUser(2L, null, "수진", AppRole.FAMILY, 12L, 100L, GroupMemberRole.FAMILY, null, "엄마"));
        when(reactionTargetDiaryPort.loadById(999L)).thenReturn(Optional.empty());

        assertThrows(DiaryNotFoundException.class, () -> createDiaryCommentService.create(2L, request));
    }

    @Test
    void createFailsWhenCommentContentIsBlank() {
        when(familyConnections.isConnected(1L, 2L)).thenReturn(true);
        CreateDiaryCommentRequest request = new CreateDiaryCommentRequest(30L, "   ");
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

        assertThrows(InvalidDiaryContentException.class, () -> createDiaryCommentService.create(2L, request));
    }
}
