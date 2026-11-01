package com.wishdiary.application.auth;

import com.wishdiary.domain.user.DuplicateUserEmailException;
import org.springframework.stereotype.Component;

@Component
public class SignupPolicy {

    public void validateEmailNotExists(String email, SignupPersistencePort signupPersistencePort) {
        if (signupPersistencePort.existsUserByEmail(email)) {
            throw new DuplicateUserEmailException(email);
        }
    }
}
