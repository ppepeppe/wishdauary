package com.wishdiary.application.diary;

import com.wishdiary.domain.diary.Diary;
import org.springframework.stereotype.Component;

@Component
public class CreateDiaryResponseMapper {

    public CreateDiaryResponse map(Diary diary) {
        return new CreateDiaryResponse(
                diary.getId(),
                diary.getAuthorId(),
                diary.getContent(),
                diary.getEmotion() == null ? null : diary.getEmotion().name().toLowerCase(),
                diary.getVisibilityScope().name().toLowerCase(),
                diary.getWrittenDate(),
                diary.getCreatedAt(),
                diary.getSharingGroupIds()
        );
    }
}
