package com.wishdiary.adapter.in.web.auth;

import com.wishdiary.application.auth.CheckSignupAvailabilityUseCase;
import com.wishdiary.common.response.CommonResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/signup/availability")
public class SignupAvailabilityController {
    private final CheckSignupAvailabilityUseCase availability;
    public SignupAvailabilityController(CheckSignupAvailabilityUseCase availability) { this.availability = availability; }

    @GetMapping
    public CommonResponse<CheckSignupAvailabilityUseCase.Availability> check(
            @RequestParam CheckSignupAvailabilityUseCase.Field field, @RequestParam String value) {
        return CommonResponse.success("Signup availability checked.", availability.check(field, value));
    }
}
