package com.wishdiary.adapter.out.persistence.reaction;

import com.wishdiary.application.reaction.HeartReactionRepository;
import com.wishdiary.domain.reaction.HeartReaction;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class HeartReactionPersistenceAdapter implements HeartReactionRepository {

    private final HeartReactionJpaRepository heartReactionJpaRepository;

    public HeartReactionPersistenceAdapter(HeartReactionJpaRepository heartReactionJpaRepository) {
        this.heartReactionJpaRepository = heartReactionJpaRepository;
    }

    @Override
    public Optional<HeartReaction> findByDiaryIdAndFamilyUserId(Long diaryId, Long familyUserId) {
        return heartReactionJpaRepository.findByDiaryIdAndFamilyUserId(diaryId, familyUserId)
                .map(this::toDomain);
    }

    @Override
    public HeartReaction save(HeartReaction heartReaction) {
        HeartReactionJpaEntity savedEntity = heartReactionJpaRepository.save(toEntity(heartReaction));
        return toDomain(savedEntity);
    }

    @Override
    public void deleteById(Long reactionId) {
        heartReactionJpaRepository.deleteById(reactionId);
    }

    @Override
    public long countByDiaryId(Long diaryId) {
        return heartReactionJpaRepository.countByDiaryId(diaryId);
    }

    @Override
    public Map<Long, Long> countByDiaryIds(List<Long> diaryIds) {
        if (diaryIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> counts = new LinkedHashMap<>();
        for (Long diaryId : diaryIds) {
            counts.put(diaryId, 0L);
        }

        heartReactionJpaRepository.countGroupedByDiaryIds(diaryIds)
                .forEach(projection -> counts.put(projection.getDiaryId(), projection.getHeartCount()));
        return counts;
    }

    @Override
    public List<HeartReaction> findAllByDiaryIds(List<Long> diaryIds) {
        if (diaryIds.isEmpty()) {
            return List.of();
        }

        return heartReactionJpaRepository.findAllByDiaryIdIn(diaryIds)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteAllByDiaryId(Long diaryId) {
        heartReactionJpaRepository.deleteAllByDiaryId(diaryId);
    }

    private HeartReactionJpaEntity toEntity(HeartReaction heartReaction) {
        return new HeartReactionJpaEntity(
                heartReaction.getId(),
                heartReaction.getDiaryId(),
                heartReaction.getFamilyUserId(),
                heartReaction.getCreatedAt()
        );
    }

    private HeartReaction toDomain(HeartReactionJpaEntity heartReactionJpaEntity) {
        return HeartReaction.reconstitute(
                heartReactionJpaEntity.getId(),
                heartReactionJpaEntity.getDiaryId(),
                heartReactionJpaEntity.getFamilyUserId(),
                heartReactionJpaEntity.getCreatedAt()
        );
    }
}
