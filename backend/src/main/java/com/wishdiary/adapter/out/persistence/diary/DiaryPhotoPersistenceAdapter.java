package com.wishdiary.adapter.out.persistence.diary;

import com.wishdiary.application.diary.DiaryPhotoPort;
import com.wishdiary.domain.diary.DiaryPhoto;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DiaryPhotoPersistenceAdapter implements DiaryPhotoPort {
    private final DiaryPhotoJpaRepository repository;
    public DiaryPhotoPersistenceAdapter(DiaryPhotoJpaRepository repository) { this.repository = repository; }
    public void save(Long diaryId, DiaryPhoto photo) { repository.save(new DiaryPhotoJpaEntity(diaryId, photo.bytes())); }
    public Optional<DiaryPhoto> load(Long diaryId) { return repository.findById(diaryId).map(entity -> new DiaryPhoto(entity.getImageData())); }
    public void delete(Long diaryId) { repository.deleteById(diaryId); }
}
