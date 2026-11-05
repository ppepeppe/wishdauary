package com.wishdiary.adapter.out.persistence.diary;

import com.wishdiary.application.diary.LoadSharedDiaryFeedPort;
import org.springframework.data.domain.PageRequest;
import com.wishdiary.application.diary.DiaryRepository;
import com.wishdiary.application.diary.LoadDiaryQueryPort;
import com.wishdiary.application.reaction.ReactionTargetDiaryPort;
import com.wishdiary.domain.diary.Diary;
import com.wishdiary.domain.diary.DiaryEmotion;
import com.wishdiary.domain.diary.DiaryVisibilityScope;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DiaryPersistenceAdapter implements DiaryRepository, LoadDiaryQueryPort, ReactionTargetDiaryPort, LoadSharedDiaryFeedPort {

    private final DiaryJpaRepository diaryJpaRepository;

    public DiaryPersistenceAdapter(DiaryJpaRepository diaryJpaRepository) {
        this.diaryJpaRepository = diaryJpaRepository;
    }

    @Override
    public List<Diary> loadSharedFeed(Long viewerId, int page) {
        return diaryJpaRepository.findSharedFeed(viewerId, PageRequest.of(page, 30))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByAuthorIdAndWrittenDate(Long authorId, LocalDate writtenDate) {
        return diaryJpaRepository.existsByAuthorIdAndWrittenDate(authorId, writtenDate);
    }

    @Override
    public long countByAuthorIdAndWrittenDate(Long authorId, LocalDate writtenDate) {
        return diaryJpaRepository.countByAuthorIdAndWrittenDate(authorId, writtenDate);
    }

    @Override
    public Optional<Diary> loadLatestByAuthorIdAndWrittenDate(Long authorId, LocalDate writtenDate) {
        return diaryJpaRepository.findTopByAuthorIdAndWrittenDateOrderByCreatedAtDescIdDesc(authorId, writtenDate)
                .map(this::toDomain);
    }

    @Override
    public Optional<Diary> loadById(Long diaryId) {
        return diaryJpaRepository.findById(diaryId)
                .map(this::toDomain);
    }

    @Override
    public List<Diary> loadAllByAuthorId(Long authorId) {
        return diaryJpaRepository.findAllByAuthorIdOrderByWrittenDateDescCreatedAtDesc(authorId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Diary> loadFamilyVisibleByAuthorIds(List<Long> authorIds) {
        if (authorIds.isEmpty()) {
            return List.of();
        }

        return diaryJpaRepository.findAllByAuthorIdInAndVisibilityScopeInOrderByWrittenDateDescCreatedAtDesc(
                        authorIds,
                        List.of(DiaryVisibilityScope.FAMILY.name(), DiaryVisibilityScope.GROUP.name(), DiaryVisibilityScope.PUBLIC.name())
                )
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Diary save(Diary diary) {
        DiaryJpaEntity diaryEntity = toEntity(diary);
        DiaryJpaEntity savedEntity = diaryJpaRepository.save(diaryEntity);
        return toDomain(savedEntity);
    }

    @Override
    public void deleteById(Long diaryId) {
        diaryJpaRepository.deleteById(diaryId);
    }

    private DiaryJpaEntity toEntity(Diary diary) {
        DiaryJpaEntity entity = new DiaryJpaEntity(
                diary.getId(),
                diary.getAuthorId(),
                diary.getContent(),
                diary.getEmotion() == null ? null : diary.getEmotion().name(),
                diary.getVisibilityScope().name(),
                diary.getWrittenDate(),
                diary.getCreatedAt()
        );
        entity.setSharingGroupIds(diary.getSharingGroupIds());
        return entity;
    }

    private Diary toDomain(DiaryJpaEntity diaryEntity) {
        return Diary.reconstitute(
                diaryEntity.getId(),
                diaryEntity.getAuthorId(),
                diaryEntity.getContent(),
                DiaryEmotion.from(diaryEntity.getEmotion()),
                DiaryVisibilityScope.from(diaryEntity.getVisibilityScope()),
                diaryEntity.getWrittenDate(),
                diaryEntity.getCreatedAt(),
                diaryEntity.getSharingGroupIds()
        );
    }
}
