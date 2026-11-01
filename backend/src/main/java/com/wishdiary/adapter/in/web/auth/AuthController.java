package com.wishdiary.adapter.in.web.auth;

import com.wishdiary.application.auth.ChildSignupRequest;
import com.wishdiary.application.auth.ChildSignupUseCase;
import com.wishdiary.application.auth.FamilySignupRequest;
import com.wishdiary.application.auth.FamilySignupUseCase;
import com.wishdiary.application.auth.LoginRequest;
import com.wishdiary.application.auth.LoginResponse;
import com.wishdiary.application.auth.LoginUseCase;
import com.wishdiary.application.auth.SignupResponse;
import com.wishdiary.common.response.CommonResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final ChildSignupUseCase childSignupUseCase;
    private final FamilySignupUseCase familySignupUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(
            ChildSignupUseCase childSignupUseCase,
            FamilySignupUseCase familySignupUseCase,
            LoginUseCase loginUseCase
    ) {
        this.childSignupUseCase = childSignupUseCase;
        this.familySignupUseCase = familySignupUseCase;
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/signup/child")
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<SignupResponse> signupChild(@Valid @RequestBody ChildSignupRequest request) {
        SignupResponse response = childSignupUseCase.signup(request);
        return CommonResponse.success("Child signup completed.", response);
    }

    @PostMapping("/signup/family")
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<SignupResponse> signupFamily(@Valid @RequestBody FamilySignupRequest request) {
        SignupResponse response = familySignupUseCase.signup(request);
        return CommonResponse.success("Family signup completed.", response);
    }

    @PostMapping("/login")
    public CommonResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginUseCase.login(request);
        return CommonResponse.success("Login completed.", response);
    }
}
