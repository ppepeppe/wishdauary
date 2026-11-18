package com.wishdiary.application.auth;

import com.wishdiary.domain.user.DuplicateLoginIdException;
import org.springframework.stereotype.Component;

@Component
public class SignupPolicy {

    public void validateLoginIdNotExists(String loginId, SignupPersistencePort signupPersistencePort) {
        if (signupPersistencePort.existsUserByLoginId(loginId)) {
            throw new DuplicateLoginIdException();
        }
    }
    public void validateNicknameNotExists(String nickname, SignupPersistencePort persistence) {
        if (persistence.existsUserByNickname(nickname)) {
            throw new com.wishdiary.domain.user.DuplicateNicknameException();
        }
    }
}
