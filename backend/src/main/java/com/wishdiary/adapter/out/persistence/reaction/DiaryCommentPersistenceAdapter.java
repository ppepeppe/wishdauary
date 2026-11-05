package com.wishdiary.adapter.out.persistence.reaction;

import com.wishdiary.application.reaction.DiaryCommentRepository;
import com.wishdiary.domain.reaction.DiaryComment;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DiaryCommentPersistenceAdapter implements DiaryCommentRepository {

    private final DiaryCommentJpaRepository diaryCommentJpaRepository;

    public DiaryCommentPersistenceAdapter(DiaryCommentJpaRepository diaryCommentJpaRepository) {
        this.diaryCommentJpaRepository = diaryCommentJpaRepository;
    }

    @Override
    public DiaryComment save(DiaryComment diaryComment) {
        DiaryCommentJpaEntity savedEntity = diaryCommentJpaRepository.save(toEntity(diaryComment));
        return toDomain(savedEntity);
    }

    @Override
    public List<DiaryComment> findAllByDiaryId(Long diaryId) {
        return diaryCommentJpaRepository.findAllByDiaryIdOrderByCreatedAtAscIdAsc(diaryId)
                .stream()
                .map(this::toDomain)
                .toList();
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

        diaryCommentJpaRepository.countGroupedByDiaryIds(diaryIds)
                .forEach(projection -> counts.put(projection.getDiaryId(), projection.getCommentCount()));
        return counts;
    }

    @Override
    public List<DiaryComment> findAllByDiaryIds(List<Long> diaryIds) {
        if (diaryIds.isEmpty()) {
            return List.of();
        }

        return diaryCommentJpaRepository.findAllByDiaryIdIn(diaryIds)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteAllByDiaryId(Long diaryId) {
        diaryCommentJpaRepository.deleteAllByDiaryId(diaryId);
    }

    private DiaryCommentJpaEntity toEntity(DiaryComment diaryComment) {
        return new DiaryCommentJpaEntity(
                diaryComment.getId(),
                diaryComment.getDiaryId(),
                diaryComment.getAuthorId(),
                diaryComment.getContent(),
                diaryComment.getCreatedAt()
        );
    }

    private DiaryComment toDomain(DiaryCommentJpaEntity diaryCommentJpaEntity) {
        return DiaryComment.reconstitute(
                diaryCommentJpaEntity.getId(),
                diaryCommentJpaEntity.getDiaryId(),
                diaryCommentJpaEntity.getAuthorId(),
                diaryCommentJpaEntity.getContent(),
                diaryCommentJpaEntity.getCreatedAt()
        );
    }
}
