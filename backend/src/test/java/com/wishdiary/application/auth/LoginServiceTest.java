package com.wishdiary.application.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wishdiary.domain.auth.InvalidLoginPasswordException;
import com.wishdiary.domain.user.AppRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private LoginPersistencePort loginPersistencePort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private AuthTokenIssuerPort authTokenIssuerPort;

    private LoginService loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginService(
                loginPersistencePort,
                passwordEncoderPort,
                authTokenIssuerPort,
                new LoginResponseMapper()
        );
    }

    @Test
    void loginReturnsResponseWithAccessToken() {
        when(loginPersistencePort.loadByEmail("child@test.com")).thenReturn(loadedLoginUser());
        when(passwordEncoderPort.matches("password", "encoded")).thenReturn(true);
        when(authTokenIssuerPort.issue(any(AuthTokenIssueRequest.class))).thenReturn("access-token");

        LoginResponse response = loginService.login(new LoginRequest("child@test.com", "password"));

        assertEquals(1L, response.userId());
        assertEquals("미아", response.nickname());
        assertEquals("access-token", response.accessToken());
    }

    @Test
    void loginFailsWhenPasswordDoesNotMatch() {
        when(loginPersistencePort.loadByEmail("child@test.com")).thenReturn(loadedLoginUser());
        when(passwordEncoderPort.matches("wrong-password", "encoded")).thenReturn(false);

        assertThrows(
                InvalidLoginPasswordException.class,
                () -> loginService.login(new LoginRequest("child@test.com", "wrong-password"))
        );

        verify(authTokenIssuerPort, never()).issue(any(AuthTokenIssueRequest.class));
    }

    private LoadedLoginUser loadedLoginUser() {
        return new LoadedLoginUser(
                1L,
                "child@test.com",
                "미아",
                AppRole.CHILD,
                "encoded",
                10L,
                "CHILD",
                null,
                "INVITE1"
        );
    }
}
