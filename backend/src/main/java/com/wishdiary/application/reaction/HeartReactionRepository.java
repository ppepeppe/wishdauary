package com.wishdiary.application.reaction;

import com.wishdiary.domain.reaction.HeartReaction;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HeartReactionRepository {

    Optional<HeartReaction> findByDiaryIdAndFamilyUserId(Long diaryId, Long familyUserId);

    HeartReaction save(HeartReaction heartReaction);

    void deleteById(Long reactionId);

    long countByDiaryId(Long diaryId);

    Map<Long, Long> countByDiaryIds(List<Long> diaryIds);

    List<HeartReaction> findAllByDiaryIds(List<Long> diaryIds);

    void deleteAllByDiaryId(Long diaryId);
}
