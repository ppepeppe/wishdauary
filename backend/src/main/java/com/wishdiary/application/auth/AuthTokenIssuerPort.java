package com.wishdiary.application.auth;

public interface AuthTokenIssuerPort {

    String issue(AuthTokenIssueRequest request);
}
