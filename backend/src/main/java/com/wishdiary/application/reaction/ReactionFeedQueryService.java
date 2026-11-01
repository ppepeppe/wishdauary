package com.wishdiary.application.reaction;

import com.wishdiary.application.diary.LoadDiaryQueryPort;
import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.diary.Diary;
import com.wishdiary.domain.reaction.DiaryComment;
import com.wishdiary.domain.reaction.HeartReaction;
import com.wishdiary.domain.user.ChildReactionFeedAccessDeniedException;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReactionFeedQueryService implements GetReactionFeedUseCase {

    private final LoadDiaryQueryPort loadDiaryQueryPort;
    private final HeartReactionRepository heartReactionRepository;
    private final DiaryCommentRepository diaryCommentRepository;
    private final LoadUserPort loadUserPort;
    private final ReactionFeedResponseMapper reactionFeedResponseMapper;
    private final com.wishdiary.application.group.FamilyConnectionPort familyConnections;

    public ReactionFeedQueryService(
            LoadDiaryQueryPort loadDiaryQueryPort,
            HeartReactionRepository heartReactionRepository,
            DiaryCommentRepository diaryCommentRepository,
            LoadUserPort loadUserPort,
            ReactionFeedResponseMapper reactionFeedResponseMapper,
            com.wishdiary.application.group.FamilyConnectionPort familyConnections
    ) {
        this.loadDiaryQueryPort = loadDiaryQueryPort;
        this.heartReactionRepository = heartReactionRepository;
        this.diaryCommentRepository = diaryCommentRepository;
        this.loadUserPort = loadUserPort;
        this.reactionFeedResponseMapper = reactionFeedResponseMapper;
        this.familyConnections = familyConnections;
    }

    @Override
    public List<ReactionFeedEventResponse> getRecentFeed(Long childUserId) {
        LoadedUser childUser = loadUserPort.loadById(childUserId);
        validateChildUser(childUser);

        List<Diary> diaries = loadDiaryQueryPort.loadAllByAuthorId(childUserId);
        List<Long> diaryIds = diaries.stream()
                .map(Diary::getId)
                .toList();

        List<HeartReaction> heartReactions = heartReactionRepository.findAllByDiaryIds(diaryIds);
        List<DiaryComment> comments = diaryCommentRepository.findAllByDiaryIds(diaryIds);
        List<Long> actorIds = java.util.stream.Stream.concat(
                        heartReactions.stream().map(HeartReaction::getFamilyUserId),
                        comments.stream().map(DiaryComment::getAuthorId)
                )
                .distinct()
                .toList();
        java.util.Map<Long, LoadedUser> actors = loadUserPort.loadByIds(actorIds);

        List<Long> connectedFamilyIds = familyConnections.findFamilyUserIds(childUserId);
        List<HeartReaction> filteredHeartReactions = heartReactions.stream()
                .filter(reaction -> isConnectedFamilyReaction(actors.get(reaction.getFamilyUserId()), connectedFamilyIds))
                .toList();
        List<DiaryComment> filteredComments = comments.stream()
                .filter(comment -> isConnectedFamilyReaction(actors.get(comment.getAuthorId()), connectedFamilyIds))
                .toList();

        return reactionFeedResponseMapper.toResponses(filteredHeartReactions, filteredComments, actors)
                .stream()
                .sorted(Comparator.comparing(ReactionFeedEventResponse::createdAt).reversed())
                .toList();
    }

    private void validateChildUser(LoadedUser childUser) {
        if (!childUser.isChild()) {
            throw new ChildReactionFeedAccessDeniedException(childUser.id());
        }
    }

    private boolean isConnectedFamilyReaction(LoadedUser actor, List<Long> connectedFamilyIds) {
        return actor != null && actor.isFamily() && connectedFamilyIds.contains(actor.id());
    }
}
