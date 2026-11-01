package com.wishdiary.application.auth;

import com.wishdiary.domain.group.Group;
import com.wishdiary.domain.group.GroupMember;
import com.wishdiary.domain.group.GroupMemberRole;
import com.wishdiary.domain.user.AppRole;
import com.wishdiary.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FamilySignupService implements FamilySignupUseCase {

    private final SignupPersistencePort signupPersistencePort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final AuthTokenIssuerPort authTokenIssuerPort;
    private final SignupPolicy signupPolicy;
    private final SignupInviteCodeGenerator signupInviteCodeGenerator;
    private final SignupResponseMapper signupResponseMapper;

    public FamilySignupService(
            SignupPersistencePort signupPersistencePort,
            PasswordEncoderPort passwordEncoderPort,
            AuthTokenIssuerPort authTokenIssuerPort,
            SignupPolicy signupPolicy,
            SignupInviteCodeGenerator signupInviteCodeGenerator,
            SignupResponseMapper signupResponseMapper
    ) {
        this.signupPersistencePort = signupPersistencePort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.authTokenIssuerPort = authTokenIssuerPort;
        this.signupPolicy = signupPolicy;
        this.signupInviteCodeGenerator = signupInviteCodeGenerator;
        this.signupResponseMapper = signupResponseMapper;
    }

    @Override
    @Transactional
    public SignupResponse signup(FamilySignupRequest request) {
        signupPolicy.validateEmailNotExists(request.email(), signupPersistencePort);
        Group group = signupPersistencePort.saveGroup(new Group(null, signupInviteCodeGenerator.generate(), true));

        String encodedPassword = passwordEncoderPort.encode(request.password());
        User user = signupPersistencePort.saveUser(
                new User(null, request.email(), request.nickname(), AppRole.FAMILY),
                encodedPassword
        );
        GroupMember groupMember = signupPersistencePort.saveGroupMember(
                new GroupMember(
                        null,
                        group.id(),
                        user.id(),
                        GroupMemberRole.FAMILY,
                        request.familyRelation(),
                        request.relationLabel()
                )
        );
        SignupResponse response = signupResponseMapper.map(user, group, groupMember);
        return withAccessToken(response);
    }

    private SignupResponse withAccessToken(SignupResponse response) {
        String accessToken = authTokenIssuerPort.issue(new AuthTokenIssueRequest(
                response.userId(),
                response.groupId(),
                response.appRole(),
                response.nickname(),
                response.relationLabel(),
                response.inviteCode()
        ));
        return new SignupResponse(
                response.userId(),
                response.groupId(),
                response.groupMemberId(),
                response.appRole(),
                response.memberRole(),
                response.nickname(),
                response.familyRelation(),
                response.relationLabel(),
                response.inviteCode(),
                accessToken
        );
    }

}
