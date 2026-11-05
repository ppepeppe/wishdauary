package com.wishdiary.adapter.out.persistence.user;

import com.wishdiary.application.user.LoadUserAccountPort;
import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.application.user.SaveUserProfilePort;
import com.wishdiary.domain.group.GroupMemberRole;
import com.wishdiary.domain.user.ProfileAvatar;
import com.wishdiary.domain.user.User;
import com.wishdiary.domain.user.UserNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceAdapter implements LoadUserPort, LoadUserAccountPort, SaveUserProfilePort {

    private final UserJpaRepository userJpaRepository;
    private final GroupMemberJpaRepository groupMemberJpaRepository;

    public UserPersistenceAdapter(
            UserJpaRepository userJpaRepository,
            GroupMemberJpaRepository groupMemberJpaRepository
    ) {
        this.userJpaRepository = userJpaRepository;
        this.groupMemberJpaRepository = groupMemberJpaRepository;
    }

    @Override
    public LoadedUser loadById(Long userId) {
        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        GroupMemberJpaEntity groupMember = groupMemberJpaRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return toLoadedUser(user, groupMember);
    }

    @Override
    public List<LoadedUser> loadChildrenByGroupId(Long groupId) {
        List<GroupMemberJpaEntity> childMembers = groupMemberJpaRepository.findAllByGroupIdAndMemberRoleOrderByUserIdAsc(
                groupId,
                GroupMemberRole.CHILD
        );
        return mapLoadedUsers(childMembers);
    }

    @Override
    public List<LoadedUser> loadFamilyMembersByGroupId(Long groupId) {
        List<GroupMemberJpaEntity> familyMembers = groupMemberJpaRepository.findAllByGroupIdAndMemberRoleOrderByUserIdAsc(
                groupId,
                GroupMemberRole.FAMILY
        );
        return mapLoadedUsers(familyMembers);
    }

    private List<LoadedUser> mapLoadedUsers(List<GroupMemberJpaEntity> groupMembers) {
        Map<Long, UserJpaEntity> usersById = userJpaRepository.findAllByIdIn(
                        groupMembers.stream().map(GroupMemberJpaEntity::getUserId).toList()
                )
                .stream()
                .collect(Collectors.toMap(UserJpaEntity::getId, Function.identity()));

        return groupMembers.stream()
                .map(groupMember -> {
                    UserJpaEntity user = usersById.get(groupMember.getUserId());
                    if (user == null) {
                        throw new UserNotFoundException(groupMember.getUserId());
                    }
                    return toLoadedUser(user, groupMember);
                })
                .toList();
    }

    @Override
    public Map<Long, LoadedUser> loadByIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, UserJpaEntity> usersById = userJpaRepository.findAllByIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(UserJpaEntity::getId, Function.identity()));
        Map<Long, GroupMemberJpaEntity> groupMembersByUserId = groupMemberJpaRepository.findAllByUserIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(GroupMemberJpaEntity::getUserId, Function.identity()));

        Map<Long, LoadedUser> loadedUsers = new LinkedHashMap<>();
        for (Long userId : userIds) {
            UserJpaEntity user = usersById.get(userId);
            GroupMemberJpaEntity groupMember = groupMembersByUserId.get(userId);
            if (user == null || groupMember == null) {
                continue;
            }
            loadedUsers.put(userId, toLoadedUser(user, groupMember));
        }
        return loadedUsers;
    }

    @Override
    public User loadAccount(Long userId) {
        return toUser(requireUser(userId));
    }

    @Override
    public User saveProfile(User user) {
        UserJpaEntity entity = requireUser(user.id());
        entity.updateProfile(user.nickname(), user.avatar().preset(), user.avatar().imageDataUrl(), user.avatar().character());
        return toUser(userJpaRepository.save(entity));
    }

    private UserJpaEntity requireUser(Long userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private User toUser(UserJpaEntity user) {
        return new User(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAppRole(),
                ProfileAvatar.of(user.getAvatarPreset(), user.getAvatarImage(), user.getAvatarCharacter())
        );
    }

    private LoadedUser toLoadedUser(UserJpaEntity user, GroupMemberJpaEntity groupMember) {
        return new LoadedUser(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAppRole(),
                groupMember.getId(),
                groupMember.getGroupId(),
                groupMember.getMemberRole(),
                groupMember.getFamilyRelation(),
                groupMember.getRelationLabel()
        );
    }
}
