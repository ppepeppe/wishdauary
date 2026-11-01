package com.wishdiary.application.reaction;

import com.wishdiary.application.diary.DiaryAudiencePolicy;
import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.diary.Diary;
import com.wishdiary.domain.diary.DiaryNotFoundException;
import com.wishdiary.domain.reaction.HeartReaction;
import com.wishdiary.domain.reaction.HeartReactionNotAllowedException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ToggleHeartReactionService implements ToggleHeartReactionUseCase {

    private final DiaryAudiencePolicy audiencePolicy;
    private final HeartReactionRepository heartReactionRepository;
    private final ReactionTargetDiaryPort reactionTargetDiaryPort;
    private final LoadUserPort loadUserPort;

    public ToggleHeartReactionService(
            HeartReactionRepository heartReactionRepository,
            ReactionTargetDiaryPort reactionTargetDiaryPort,
            LoadUserPort loadUserPort,
            DiaryAudiencePolicy audiencePolicy
    ) {
        this.heartReactionRepository = heartReactionRepository;
        this.reactionTargetDiaryPort = reactionTargetDiaryPort;
        this.loadUserPort = loadUserPort;
        this.audiencePolicy = audiencePolicy;
    }

    @Override
    @Transactional
    public ToggleHeartReactionResponse toggle(Long familyUserId, ToggleHeartReactionRequest request) {
        LoadedUser familyUser = loadUserPort.loadById(familyUserId);
        validateFamilyUserRole(familyUser);

        Diary diary = reactionTargetDiaryPort.loadById(request.diaryId())
                .orElseThrow(() -> new DiaryNotFoundException(request.diaryId()));

        LoadedUser diaryAuthor = loadUserPort.loadById(diary.getAuthorId());
        validateReactionTarget(familyUser, diary, diaryAuthor);

        Optional<HeartReaction> existingReaction = heartReactionRepository.findByDiaryIdAndFamilyUserId(
                request.diaryId(),
                familyUserId
        );

        boolean hearted = existingReaction.isEmpty();
        if (hearted) {
            heartReactionRepository.save(HeartReaction.create(request.diaryId(), familyUserId));
        } else {
            heartReactionRepository.deleteById(existingReaction.get().getId());
        }

        long heartCount = heartReactionRepository.countByDiaryId(request.diaryId());
        return new ToggleHeartReactionResponse(
                request.diaryId(),
                familyUser.id(),
                familyUser.nickname(),
                familyUser.relationLabel(),
                hearted,
                heartCount
        );
    }

    private void validateFamilyUserRole(LoadedUser familyUser) {
        if (!familyUser.isFamily()) {
            throw new HeartReactionNotAllowedException();
        }
    }

    private void validateReactionTarget(LoadedUser familyUser, Diary diary, LoadedUser diaryAuthor) {
        if (!audiencePolicy.canRead(familyUser, diary, diaryAuthor)) {
            throw new HeartReactionNotAllowedException();
        }
    }
}
