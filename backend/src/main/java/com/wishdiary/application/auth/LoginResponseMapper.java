package com.wishdiary.application.auth;
import org.springframework.stereotype.Component;

@Component
public class LoginResponseMapper {

    public LoginResponse map(LoadedLoginUser user) {
        String appRole = user.appRole().name().toLowerCase();
        return new LoginResponse(
                user.userId(),
                user.groupId(),
                appRole,
                user.memberRole().toLowerCase(),
                user.nickname(),
                user.relationLabel(),
                null,
                null
        );
    }
}
