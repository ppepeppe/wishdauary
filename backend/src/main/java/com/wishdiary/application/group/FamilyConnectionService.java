package com.wishdiary.application.group;

import com.wishdiary.application.sharing.*;
import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.group.*;
import com.wishdiary.domain.sharing.SharingGroup;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FamilyConnectionService implements ManageFamilyConnectionsUseCase {
    private final FamilyConnectionRequestPort requests;
    private final FamilyConnectionPort connections;
    private final SharingGroupPort groups;
    private final SharingUserDirectoryPort directory;
    private final LoadUserPort users;
    public FamilyConnectionService(FamilyConnectionRequestPort requests, FamilyConnectionPort connections,
            SharingGroupPort groups, SharingUserDirectoryPort directory, LoadUserPort users) {
        this.requests = requests; this.connections = connections; this.groups = groups;
        this.directory = directory; this.users = users;
    }
    public List<SharingUser> searchChildren(Long familyId, String query) {
        requireFamily(familyId);
        if (query == null || query.isBlank()) return List.of();
        if (query.trim().length() < 2 || query.trim().length() > 50) {
            throw new InvalidFamilyConnectionRequestException("아이 닉네임을 2~50자로 입력해 주세요.");
        }
        return directory.searchChildren(query.trim());
    }
    public List<FamilyConnectionUserResponse> listConnections(Long userId) {
        LoadedUser user = users.loadById(userId);
        List<Long> ids = user.isFamily() ? connections.findChildUserIds(userId) : connections.findFamilyUserIds(userId);
        return ids.stream().map(users.loadByIds(ids)::get)
                .map(person -> new FamilyConnectionUserResponse(person.id(), person.nickname(), person.relationLabel())).toList();
    }
    public List<FamilyConnectionRequestResponse> listRequests(Long userId) {
        LoadedUser user = users.loadById(userId);
        List<FamilyConnectionRequest> found = user.isFamily() ? requests.findByFamilyId(userId) : requests.findByChildId(userId);
        return found.stream().map(this::response).toList();
    }
    @Transactional
    public FamilyConnectionRequestResponse request(Long familyId, Long childId) {
        requireFamily(familyId);
        if (!users.loadById(childId).isChild()) throw new InvalidFamilyConnectionRequestException("아이 계정을 선택해 주세요.");
        groups.lockOwner(childId);
        if (connections.isConnected(childId, familyId)) throw new FamilyConnectionRequestConflictException("이미 연결된 아이예요.");
        FamilyConnectionRequest existing = requests.findByPair(familyId, childId).orElse(null);
        if (existing != null && existing.status() == FamilyConnectionRequest.Status.PENDING) return response(existing);
        FamilyConnectionRequest next = existing == null ? FamilyConnectionRequest.create(familyId, childId) : existing.renew();
        return response(requests.save(next));
    }
    @Transactional
    public FamilyConnectionRequestResponse process(Long childId, Long requestId, ProcessFamilyConnectionRequest command) {
        if (!users.loadById(childId).isChild()) throw new FamilyConnectionAccessDeniedException();
        groups.lockOwner(childId);
        FamilyConnectionRequest request = requests.findById(requestId).filter(value -> value.childUserId().equals(childId))
                .orElseThrow(FamilyConnectionAccessDeniedException::new);
        FamilyConnectionRequest processed = request.process(command.action() == ProcessFamilyConnectionRequest.Action.APPROVE);
        if (processed.status() == FamilyConnectionRequest.Status.APPROVED) connect(request);
        return response(requests.save(processed));
    }
    private void connect(FamilyConnectionRequest request) {
        SharingGroup group = groups.findActiveByOwnerId(request.childUserId()).stream()
                .filter(value -> Integer.valueOf(SharingGroup.FAMILY_GROUP_SLOT).equals(value.slot())).findFirst()
                .orElseGet(() -> new SharingGroup(null, request.childUserId(), "우리 가족", SharingGroup.FAMILY_GROUP_SLOT, java.util.Set.of()));
        var members = new HashSet<>(group.memberIds());
        members.add(request.familyUserId());
        groups.save(group.update(group.name(), members));
    }
    private void requireFamily(Long userId) {
        if (!users.loadById(userId).isFamily()) throw new FamilyConnectionAccessDeniedException();
    }
    private FamilyConnectionRequestResponse response(FamilyConnectionRequest request) {
        Map<Long, LoadedUser> people = users.loadByIds(List.of(request.familyUserId(), request.childUserId()));
        LoadedUser family = people.get(request.familyUserId());
        return new FamilyConnectionRequestResponse(request.id(), request.familyUserId(), family.nickname(), family.relationLabel(),
                request.childUserId(), people.get(request.childUserId()).nickname(), request.status().name().toLowerCase(), request.createdAt(), request.processedAt());
    }
}
