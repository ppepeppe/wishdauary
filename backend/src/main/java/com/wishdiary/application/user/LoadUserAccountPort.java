package com.wishdiary.application.user;

import com.wishdiary.domain.user.User;

public interface LoadUserAccountPort {

    User loadAccount(Long userId);
}
