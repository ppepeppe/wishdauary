package com.wishdiary.application.auth;

public interface AuthTokenVerifierPort {

    AuthenticatedSession verify(String token);
}
