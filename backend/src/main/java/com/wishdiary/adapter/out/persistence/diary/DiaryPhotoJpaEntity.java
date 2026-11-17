package com.wishdiary.adapter.out.persistence.diary;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity @Table(name = "diary_photos")
public class DiaryPhotoJpaEntity {
    @Id private Long diaryId;
    @Column(nullable = false, columnDefinition = "bytea") private byte[] imageData;
    protected DiaryPhotoJpaEntity() { }
    public DiaryPhotoJpaEntity(Long diaryId, byte[] imageData) { this.diaryId = diaryId; this.imageData = imageData; }
    public byte[] getImageData() { return imageData; }
}
