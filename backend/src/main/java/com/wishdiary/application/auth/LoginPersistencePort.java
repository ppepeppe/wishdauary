package com.wishdiary.application.auth;

public interface LoginPersistencePort {

    LoadedLoginUser loadByLoginId(String loginId);
}
