package com.wishdiary.application.auth;

public interface CheckSignupAvailabilityUseCase {
    enum Field { LOGIN_ID, NICKNAME }
    record Availability(boolean available, String message) {}
    Availability check(Field field, String value);
}
