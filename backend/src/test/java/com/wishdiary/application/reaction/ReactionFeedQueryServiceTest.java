package com.wishdiary.application.reaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.wishdiary.application.diary.LoadDiaryQueryPort;
import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.group.GroupMemberRole;
import com.wishdiary.domain.user.AppRole;
import com.wishdiary.domain.diary.Diary;
import com.wishdiary.domain.diary.DiaryEmotion;
import com.wishdiary.domain.diary.DiaryVisibilityScope;
import com.wishdiary.domain.reaction.DiaryComment;
import com.wishdiary.domain.reaction.HeartReaction;
import com.wishdiary.domain.user.ChildReactionFeedAccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReactionFeedQueryServiceTest {

    @Mock
    private LoadDiaryQueryPort loadDiaryQueryPort;

    @Mock
    private HeartReactionRepository heartReactionRepository;

    @Mock
    private DiaryCommentRepository diaryCommentRepository;

    @Mock
    private LoadUserPort loadUserPort;

    @Mock
    private com.wishdiary.application.group.FamilyConnectionPort familyConnections;

    private ReactionFeedQueryService reactionFeedQueryService;

    @BeforeEach
    void setUp() {
        reactionFeedQueryService = new ReactionFeedQueryService(
                loadDiaryQueryPort,
                heartReactionRepository,
                diaryCommentRepository,
                loadUserPort,
                new ReactionFeedResponseMapper(),
                familyConnections
        );
    }

    @Test
    void getRecentFeedReturnsHeartAndCommentEventsInLatestOrder() {
        Diary diary = Diary.reconstitute(
                30L,
                1L,
                "family diary",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.FAMILY,
                LocalDate.of(2026, 4, 12),
                LocalDateTime.of(2026, 4, 12, 9, 0)
        );
        HeartReaction heartReaction = HeartReaction.reconstitute(
                101L,
                30L,
                2L,
                LocalDateTime.of(2026, 4, 12, 11, 0)
        );
        DiaryComment diaryComment = DiaryComment.reconstitute(
                201L,
                30L,
                3L,
                "정말 멋진 하루였겠다",
                LocalDateTime.of(2026, 4, 12, 12, 0)
        );

        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, "민준", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));
        when(loadDiaryQueryPort.loadAllByAuthorId(1L)).thenReturn(List.of(diary));
        when(heartReactionRepository.findAllByDiaryIds(List.of(30L))).thenReturn(List.of(heartReaction));
        when(diaryCommentRepository.findAllByDiaryIds(List.of(30L))).thenReturn(List.of(diaryComment));
        when(loadUserPort.loadByIds(List.of(2L, 3L))).thenReturn(java.util.Map.of(
                2L, new LoadedUser(2L, null, "수진", AppRole.FAMILY, 12L, 100L, GroupMemberRole.FAMILY, null, "엄마"),
                3L, new LoadedUser(3L, null, "유리", AppRole.FAMILY, 13L, 100L, GroupMemberRole.FAMILY, null, "이모")
        ));

        when(familyConnections.findFamilyUserIds(1L)).thenReturn(List.of(2L, 3L));
        List<ReactionFeedEventResponse> responses = reactionFeedQueryService.getRecentFeed(1L);

        assertEquals(2, responses.size());
        assertEquals("comment", responses.get(0).type());
        assertEquals(30L, responses.get(0).diaryId());
        assertEquals(3L, responses.get(0).actorUserId());
        assertEquals("유리", responses.get(0).actorNickname());
        assertEquals("이모", responses.get(0).actorRelationLabel());
        assertEquals("정말 멋진 하루였겠다", responses.get(0).contentPreview());
        assertEquals("heart", responses.get(1).type());
        assertEquals(2L, responses.get(1).actorUserId());
        assertEquals("수진", responses.get(1).actorNickname());
        assertEquals("엄마", responses.get(1).actorRelationLabel());
        assertNull(responses.get(1).contentPreview());
    }

    @Test
    void getRecentFeedFiltersOutNonFamilyOrDisconnectedActors() {
        Diary diary = Diary.reconstitute(
                30L,
                1L,
                "family diary",
                DiaryEmotion.HAPPY,
                DiaryVisibilityScope.FAMILY,
                LocalDate.of(2026, 4, 12),
                LocalDateTime.of(2026, 4, 12, 9, 0)
        );
        HeartReaction sameGroupHeart = HeartReaction.reconstitute(
                101L,
                30L,
                2L,
                LocalDateTime.of(2026, 4, 12, 11, 0)
        );
        HeartReaction differentGroupHeart = HeartReaction.reconstitute(
                102L,
                30L,
                5L,
                LocalDateTime.of(2026, 4, 12, 12, 0)
        );
        DiaryComment childComment = DiaryComment.reconstitute(
                201L,
                30L,
                4L,
                "나도 재밌었어",
                LocalDateTime.of(2026, 4, 12, 13, 0)
        );

        when(loadUserPort.loadById(1L)).thenReturn(new LoadedUser(1L, null, "민준", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null));
        when(loadDiaryQueryPort.loadAllByAuthorId(1L)).thenReturn(List.of(diary));
        when(heartReactionRepository.findAllByDiaryIds(List.of(30L))).thenReturn(List.of(sameGroupHeart, differentGroupHeart));
        when(diaryCommentRepository.findAllByDiaryIds(List.of(30L))).thenReturn(List.of(childComment));
        when(loadUserPort.loadByIds(List.of(2L, 5L, 4L))).thenReturn(java.util.Map.of(
                2L, new LoadedUser(2L, null, "수진", AppRole.FAMILY, 12L, 100L, GroupMemberRole.FAMILY, null, "엄마"),
                5L, new LoadedUser(5L, null, "현우", AppRole.FAMILY, 21L, 200L, GroupMemberRole.FAMILY, null, "아빠"),
                4L, new LoadedUser(4L, null, "서윤", AppRole.CHILD, 14L, 100L, GroupMemberRole.CHILD, null, null)
        ));

        when(familyConnections.findFamilyUserIds(1L)).thenReturn(List.of(2L));
        List<ReactionFeedEventResponse> responses = reactionFeedQueryService.getRecentFeed(1L);

        assertEquals(1, responses.size());
        assertEquals("heart", responses.get(0).type());
        assertEquals(2L, responses.get(0).actorUserId());
    }

    @Test
    void getRecentFeedFailsWhenTargetUserIsNotChild() {
        when(loadUserPort.loadById(2L)).thenReturn(new LoadedUser(2L, null, "수진", AppRole.FAMILY, 12L, 100L, GroupMemberRole.FAMILY, null, "엄마"));

        assertThrows(ChildReactionFeedAccessDeniedException.class, () -> reactionFeedQueryService.getRecentFeed(2L));
    }
}
