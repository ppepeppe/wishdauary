package com.wishdiary.application.diary;

import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.diary.Diary;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class DiaryQueryResponseMapper {

    private static final int CONTENT_PREVIEW_LENGTH = 40;

    public TodayDiaryExistenceResponse toTodayExistenceResponse(
            Diary diary,
            long count,
            Map<Long, Long> heartCounts,
            Map<Long, Long> commentCounts
    ) {
        return new TodayDiaryExistenceResponse(
                true,
                count,
                diary.getId(),
                diary.getEmotion() == null ? null : diary.getEmotion().name().toLowerCase(),
                heartCounts.getOrDefault(diary.getId(), 0L),
                commentCounts.getOrDefault(diary.getId(), 0L)
        );
    }

    public TodayDiaryExistenceResponse toNotFoundTodayExistenceResponse() {
        return new TodayDiaryExistenceResponse(false, 0L, null, null, 0L, 0L);
    }

    public List<MyDiarySummaryResponse> toMyDiarySummaryResponses(
            List<Diary> diaries,
            Map<Long, Long> heartCounts,
            Map<Long, Long> commentCounts
    ) {
        return diaries.stream()
                .map(diary -> toMyDiarySummaryResponse(diary, heartCounts, commentCounts))
                .toList();
    }

    public List<FamilyDiaryFeedResponse> toFamilyDiaryFeedResponses(
            List<Diary> diaries,
            Map<Long, Long> heartCounts,
            Map<Long, Long> commentCounts,
            Set<Long> heartedDiaryIds,
            Map<Long, LoadedUser> authors
    ) {
        return diaries.stream()
                .map(diary -> toFamilyDiaryFeedResponse(diary, heartCounts, commentCounts, heartedDiaryIds, authors))
                .toList();
    }

    private MyDiarySummaryResponse toMyDiarySummaryResponse(
            Diary diary,
            Map<Long, Long> heartCounts,
            Map<Long, Long> commentCounts
    ) {
        return new MyDiarySummaryResponse(
                diary.getId(),
                createContentPreview(diary.getContent()),
                diary.getEmotion() == null ? null : diary.getEmotion().name().toLowerCase(),
                diary.getVisibilityScope().name().toLowerCase(),
                heartCounts.getOrDefault(diary.getId(), 0L),
                commentCounts.getOrDefault(diary.getId(), 0L),
                diary.getWrittenDate(),
                diary.getCreatedAt(),
                diary.getSharingGroupIds()
        );
    }

    private FamilyDiaryFeedResponse toFamilyDiaryFeedResponse(
            Diary diary,
            Map<Long, Long> heartCounts,
            Map<Long, Long> commentCounts,
            Set<Long> heartedDiaryIds,
            Map<Long, LoadedUser> authors
    ) {
        Map<Long, Long> safeHeartCounts = heartCounts == null ? Collections.emptyMap() : heartCounts;
        Map<Long, Long> safeCommentCounts = commentCounts == null ? Collections.emptyMap() : commentCounts;
        Set<Long> safeHeartedDiaryIds = heartedDiaryIds == null ? Collections.emptySet() : heartedDiaryIds;
        LoadedUser author = authors.get(diary.getAuthorId());
        return new FamilyDiaryFeedResponse(
                diary.getId(),
                diary.getAuthorId(),
                author == null ? null : author.nickname(),
                author == null ? null : author.relationLabel(),
                createContentPreview(diary.getContent()),
                diary.getEmotion() == null ? null : diary.getEmotion().name().toLowerCase(),
                safeHeartedDiaryIds.contains(diary.getId()),
                safeHeartCounts.getOrDefault(diary.getId(), 0L),
                safeCommentCounts.getOrDefault(diary.getId(), 0L),
                diary.getWrittenDate(),
                diary.getCreatedAt(),
                diary.getVisibilityScope().name().toLowerCase()
        );
    }

    private String createContentPreview(String content) {
        if (content.length() <= CONTENT_PREVIEW_LENGTH) {
            return content;
        }

        return content.substring(0, CONTENT_PREVIEW_LENGTH) + "...";
    }
}
