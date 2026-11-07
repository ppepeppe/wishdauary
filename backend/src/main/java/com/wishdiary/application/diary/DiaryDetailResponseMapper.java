package com.wishdiary.application.diary;

import com.wishdiary.domain.diary.Diary;
import org.springframework.stereotype.Component;

@Component
public class DiaryDetailResponseMapper {

    public DiaryDetailResponse toResponse(Diary diary, long heartCount, long commentCount, boolean hearted) {
        return new DiaryDetailResponse(
                diary.getId(),
                diary.getAuthorId(),
                diary.getContent(),
                diary.getEmotion() == null ? null : diary.getEmotion().name().toLowerCase(),
                diary.getVisibilityScope().name().toLowerCase(),
                heartCount,
                commentCount,
                diary.getWrittenDate(),
                diary.getCreatedAt(),
                diary.getSharingGroupIds(),
                hearted
        );
    }
}
