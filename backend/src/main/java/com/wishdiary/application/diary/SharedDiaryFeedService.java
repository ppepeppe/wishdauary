package com.wishdiary.application.diary;

import com.wishdiary.application.sharing.SharingUser;
import com.wishdiary.application.sharing.SharingUserDirectoryPort;
import com.wishdiary.domain.diary.Diary;
import com.wishdiary.domain.sharing.InvalidSharingGroupException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SharedDiaryFeedService implements GetSharedDiaryFeedUseCase {
    private final LoadSharedDiaryFeedPort diaries;
    private final SharingUserDirectoryPort users;

    public SharedDiaryFeedService(LoadSharedDiaryFeedPort diaries, SharingUserDirectoryPort users) {
        this.diaries = diaries;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<SharedDiaryResponse> getSharedFeed(Long viewerId, int page) {
        if (page < 0 || page > 10000) throw new InvalidSharingGroupException("페이지 번호를 확인해 주세요.");
        List<Diary> items = diaries.loadSharedFeed(viewerId, page);
        Map<Long, String> nicknames = users.findByIds(items.stream().map(Diary::getAuthorId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(SharingUser::userId, SharingUser::nickname));
        return items.stream().map(diary -> new SharedDiaryResponse(diary.getId(),
                nicknames.getOrDefault(diary.getAuthorId(), "아이"),
                preview(diary.getContent()), diary.getEmotion() == null ? null : diary.getEmotion().name().toLowerCase(),
                diary.getVisibilityScope().name().toLowerCase(), diary.getWrittenDate(), diary.getCreatedAt())).toList();
    }

    private String preview(String content) {
        return content.length() > 120 ? content.substring(0, 120) + "…" : content;
    }
}
