package com.wishdiary.application.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SignupAvailabilityService implements CheckSignupAvailabilityUseCase {
    private final SignupPersistencePort users;
    public SignupAvailabilityService(SignupPersistencePort users) { this.users = users; }

    public Availability check(Field field, String value) {
        String cleaned = value == null ? "" : value.strip();
        if (cleaned.isBlank()) return new Availability(false, "값을 입력해 주세요.");
        if (field == Field.LOGIN_ID) {
            if (cleaned.length() > 255 || cleaned.matches("(?U).*\\s.*")) {
                return new Availability(false, "아이디는 공백 없이 255자 이내로 입력해 주세요.");
            }
            boolean available = !users.existsUserByLoginId(cleaned);
            return new Availability(available, available ? "사용 가능한 아이디예요." : "이미 사용 중인 아이디예요.");
        }
        if (cleaned.length() > 20) return new Availability(false, "닉네임은 20자 이내로 입력해 주세요.");
        boolean available = !users.existsUserByNickname(cleaned);
        return new Availability(available, available ? "사용 가능한 닉네임이에요." : "이미 사용 중인 닉네임이에요.");
    }
}
