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
    private final com.wishdiary.application.sharing.SharingGroupPort groups;

    public SharedDiaryFeedService(LoadSharedDiaryFeedPort diaries, SharingUserDirectoryPort users, com.wishdiary.application.sharing.SharingGroupPort groups) {
        this.diaries = diaries;
        this.users = users; this.groups = groups;
    }

    @Transactional(readOnly = true)
    public List<SharedDiaryResponse> getSharedFeed(Long viewerId, int page) {
        return getSharedFeed(viewerId, page, null);
    }

    @Transactional(readOnly = true)
    public List<SharedDiaryResponse> getSharedFeed(Long viewerId, int page, Long groupId) {
        if (groupId != null && groupId < -1) throw new InvalidSharingGroupException("그룹을 확인해 주세요.");
        if (page < 0 || page > 10000) throw new InvalidSharingGroupException("페이지 번호를 확인해 주세요.");
        List<Diary> items = groupId == null ? diaries.loadSharedFeed(viewerId, page) : diaries.loadSharedFeed(viewerId, page, groupId);
        return responses(items);
    }

    @Transactional(readOnly = true)
    public List<SharedDiaryResponse> getGroupedFeed(Long viewerId, int page, List<Long> groupIds) {
        if (page < 0 || page > 10000) throw new InvalidSharingGroupException("페이지 번호를 확인해 주세요.");
        if (groupIds == null || groupIds.isEmpty() || groupIds.size() > 100
                || groupIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new InvalidSharingGroupException("그룹을 확인해 주세요.");
        }
        return responses(diaries.loadGroupedFeed(viewerId, page, groupIds.stream().distinct().toList()));
    }

    private List<SharedDiaryResponse> responses(List<Diary> items) {
        Map<Long, String> nicknames = users.findByIds(items.stream().map(Diary::getAuthorId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(SharingUser::userId, SharingUser::nickname));
        return items.stream().map(diary -> new SharedDiaryResponse(diary.getId(),
                nicknames.getOrDefault(diary.getAuthorId(), "아이"),
                preview(diary.getContent()), diary.getEmotion() == null ? null : diary.getEmotion().name().toLowerCase(),
                diary.getVisibilityScope().name().toLowerCase(), diary.getWrittenDate(), diary.getCreatedAt())).toList();
    }

    @Transactional(readOnly = true)
    public List<SharedFeedGroupResponse> getGroups(Long viewerId) {
        var visible = groups.findVisible(viewerId);
        var owners = users.findByIds(visible.stream().map(com.wishdiary.domain.sharing.SharingGroup::ownerId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(SharingUser::userId, SharingUser::nickname));
        return visible.stream().map(group -> new SharedFeedGroupResponse(group.id(), group.name(), owners.getOrDefault(group.ownerId(), "아이"))).toList();
    }

    private String preview(String content) {
        return content.length() > 120 ? content.substring(0, 120) + "…" : content;
    }
}
