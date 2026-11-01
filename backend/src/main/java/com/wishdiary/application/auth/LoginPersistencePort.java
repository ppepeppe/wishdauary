package com.wishdiary.application.auth;

public interface LoginPersistencePort {

    LoadedLoginUser loadByEmail(String email);
}
