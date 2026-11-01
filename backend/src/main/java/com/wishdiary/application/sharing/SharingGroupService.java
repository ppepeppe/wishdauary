package com.wishdiary.application.sharing;

import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.domain.sharing.InvalidSharingGroupException;
import com.wishdiary.domain.sharing.SharingGroup;
import com.wishdiary.domain.sharing.SharingGroupAccessDeniedException;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SharingGroupService implements ManageSharingGroupsUseCase {
    private final SharingGroupPort groups;
    private final SharingUserDirectoryPort directory;
    private final LoadUserPort users;
    private final com.wishdiary.application.group.FamilyConnectionRequestPort connectionRequests;

    public SharingGroupService(SharingGroupPort groups, SharingUserDirectoryPort directory, LoadUserPort users,
            com.wishdiary.application.group.FamilyConnectionRequestPort connectionRequests) {
        this.groups = groups;
        this.directory = directory;
        this.users = users;
        this.connectionRequests = connectionRequests;
    }

    public List<SharingGroupResponse> list(Long childId) {
        validateChild(childId);
        return groups.findActiveByOwnerId(childId).stream().map(this::toResponse).toList();
    }

    public List<SharingUser> searchUsers(Long childId, String query) {
        validateChild(childId);
        if (query == null || query.trim().isEmpty()) return List.of();
        if (query.trim().length() > 50) throw new InvalidSharingGroupException("검색어는 50자 이내로 입력해 주세요.");
        return directory.search(query.trim(), childId);
    }

    @Transactional
    public SharingGroupResponse create(Long childId, SharingGroupRequest request) {
        validateChild(childId);
        groups.lockOwner(childId);
        List<Integer> usedSlots = groups.findActiveByOwnerId(childId).stream().map(SharingGroup::slot).toList();
        int slot = IntStream.rangeClosed(1, SharingGroup.MAX_GROUPS).filter(value -> !usedSlots.contains(value))
                .findFirst().orElseThrow(() -> new InvalidSharingGroupException("공개 그룹은 최대 5개까지 만들 수 있어요."));
        validateMembers(childId, request.memberIds());
        SharingGroup saved = groups.save(new SharingGroup(null, childId, request.name(), slot, request.memberIds()));
        approveSelectedFamilies(childId, request.memberIds());
        return toResponse(saved);
    }

    @Transactional
    public SharingGroupResponse update(Long childId, Long groupId, SharingGroupRequest request) {
        validateChild(childId);
        groups.lockOwner(childId);
        SharingGroup group = ownedGroup(childId, groupId);
        validateMembers(childId, request.memberIds());
        SharingGroup saved = groups.save(group.update(request.name(), request.memberIds()));
        approveSelectedFamilies(childId, request.memberIds());
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long childId, Long groupId) {
        validateChild(childId);
        groups.lockOwner(childId);
        groups.save(ownedGroup(childId, groupId).delete());
    }

    private void validateChild(Long childId) {
        if (!users.loadById(childId).isChild()) throw new SharingGroupAccessDeniedException();
    }

    private void approveSelectedFamilies(Long childId, Set<Long> memberIds) {
        for (Long memberId : memberIds) {
            connectionRequests.findByPair(memberId, childId)
                    .filter(request -> request.status() == com.wishdiary.domain.group.FamilyConnectionRequest.Status.PENDING)
                    .ifPresent(request -> connectionRequests.save(request.process(true)));
        }
    }

    private SharingGroup ownedGroup(Long childId, Long groupId) {
        return groups.findById(groupId).filter(group -> group.active() && group.ownerId().equals(childId))
                .orElseThrow(SharingGroupAccessDeniedException::new);
    }

    private void validateMembers(Long childId, Set<Long> memberIds) {
        if (memberIds == null || memberIds.contains(childId) || memberIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new InvalidSharingGroupException("구성원을 다시 선택해 주세요. 본인은 항상 자신의 일기를 볼 수 있어요.");
        }
        if (directory.findByIds(memberIds).size() != memberIds.size()) {
            throw new InvalidSharingGroupException("존재하지 않는 사용자가 포함되어 있어요.");
        }
    }

    private SharingGroupResponse toResponse(SharingGroup group) {
        return new SharingGroupResponse(group.id(), group.name(), group.memberIds(), directory.findByIds(group.memberIds()), Integer.valueOf(SharingGroup.FAMILY_GROUP_SLOT).equals(group.slot()));
    }
}
