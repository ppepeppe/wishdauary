package com.wishdiary.application.auth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService implements LoginUseCase {

    private final LoginPersistencePort loginPersistencePort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final AuthTokenIssuerPort authTokenIssuerPort;
    private final LoginResponseMapper loginResponseMapper;

    public LoginService(
            LoginPersistencePort loginPersistencePort,
            PasswordEncoderPort passwordEncoderPort,
            AuthTokenIssuerPort authTokenIssuerPort,
            LoginResponseMapper loginResponseMapper
    ) {
        this.loginPersistencePort = loginPersistencePort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.authTokenIssuerPort = authTokenIssuerPort;
        this.loginResponseMapper = loginResponseMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        LoadedLoginUser user = loginPersistencePort.loadByEmail(request.email());
        validatePassword(request.password(), user.passwordHash());

        LoginResponse response = loginResponseMapper.map(user);
        return withAccessToken(response);
    }

    private LoginResponse withAccessToken(LoginResponse response) {
        String accessToken = authTokenIssuerPort.issue(new AuthTokenIssueRequest(
                response.userId(),
                response.groupId(),
                response.appRole(),
                response.nickname(),
                response.relationLabel(),
                response.inviteCode()
        ));
        return new LoginResponse(
                response.userId(),
                response.groupId(),
                response.appRole(),
                response.memberRole(),
                response.nickname(),
                response.relationLabel(),
                response.inviteCode(),
                accessToken
        );
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoderPort.matches(rawPassword, encodedPassword)) {
            throw new com.wishdiary.domain.auth.InvalidLoginPasswordException();
        }
    }
}
