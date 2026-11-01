package com.wishdiary.application.group;

import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.group.FamilyManagementAccessDeniedException;
import com.wishdiary.domain.group.FamilyMemberNotFoundException;
import com.wishdiary.domain.group.Group;
import com.wishdiary.domain.group.InviteCodeGenerationFailedException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FamilyManagementService implements GetFamilyMembersUseCase, GetFamilyInviteCodeUseCase, RegenerateFamilyInviteCodeUseCase, RevokeFamilyInviteCodeUseCase, RemoveFamilyMemberUseCase {

    private static final int MAX_INVITE_CODE_GENERATION_ATTEMPTS = 10;

    private final LoadUserPort loadUserPort;
    private final FamilyGroupManagementPort familyGroupManagementPort;
    private final FamilyInviteCodeGenerator familyInviteCodeGenerator;
    private final FamilyMemberResponseMapper familyMemberResponseMapper;

    public FamilyManagementService(
            LoadUserPort loadUserPort,
            FamilyGroupManagementPort familyGroupManagementPort,
            FamilyInviteCodeGenerator familyInviteCodeGenerator,
            FamilyMemberResponseMapper familyMemberResponseMapper
    ) {
        this.loadUserPort = loadUserPort;
        this.familyGroupManagementPort = familyGroupManagementPort;
        this.familyInviteCodeGenerator = familyInviteCodeGenerator;
        this.familyMemberResponseMapper = familyMemberResponseMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FamilyMemberResponse> getMembers(Long requesterUserId) {
        LoadedUser childUser = loadChildUser(requesterUserId);
        return familyMemberResponseMapper.toResponses(loadUserPort.loadFamilyMembersByGroupId(childUser.groupId()));
    }

    @Override
    @Transactional(readOnly = true)
    public FamilyInviteCodeResponse getInviteCode(Long requesterUserId) {
        LoadedUser childUser = loadChildUser(requesterUserId);
        Group group = familyGroupManagementPort.loadGroupById(childUser.groupId());
        return toInviteCodeResponse(group);
    }

    @Override
    @Transactional
    public FamilyInviteCodeResponse regenerateInviteCode(Long requesterUserId) {
        LoadedUser childUser = loadChildUser(requesterUserId);
        Group group = familyGroupManagementPort.loadGroupById(childUser.groupId());
        Group savedGroup = familyGroupManagementPort.saveGroup(group.regenerateInviteCode(generateUniqueInviteCode()));
        return toInviteCodeResponse(savedGroup);
    }

    @Override
    @Transactional
    public FamilyInviteCodeResponse revokeInviteCode(Long requesterUserId) {
        LoadedUser childUser = loadChildUser(requesterUserId);
        Group group = familyGroupManagementPort.loadGroupById(childUser.groupId());
        Group savedGroup = familyGroupManagementPort.saveGroup(group.revokeInviteCode());
        return toInviteCodeResponse(savedGroup);
    }

    @Override
    @Transactional
    public void removeMember(Long requesterUserId, Long groupMemberId) {
        LoadedUser childUser = loadChildUser(requesterUserId);
        LoadedUser familyMember = familyGroupManagementPort.loadFamilyMemberByGroupMemberId(groupMemberId)
                .orElseThrow(() -> new FamilyMemberNotFoundException(groupMemberId));
        if (!familyMember.isFamily() || !childUser.groupId().equals(familyMember.groupId())) {
            throw new FamilyMemberNotFoundException(groupMemberId);
        }
        familyGroupManagementPort.deleteFamilyMemberByGroupMemberId(groupMemberId);
    }

    private LoadedUser loadChildUser(Long requesterUserId) {
        LoadedUser requester = loadUserPort.loadById(requesterUserId);
        if (!requester.isChild()) {
            throw new FamilyManagementAccessDeniedException();
        }
        return requester;
    }

    private String generateUniqueInviteCode() {
        for (int attempt = 0; attempt < MAX_INVITE_CODE_GENERATION_ATTEMPTS; attempt++) {
            String inviteCode = familyInviteCodeGenerator.generate();
            if (!familyGroupManagementPort.existsByInviteCode(inviteCode)) {
                return inviteCode;
            }
        }
        throw new InviteCodeGenerationFailedException();
    }

    private FamilyInviteCodeResponse toInviteCodeResponse(Group group) {
        return new FamilyInviteCodeResponse(group.inviteCode(), !group.inviteCodeRevoked());
    }
}
