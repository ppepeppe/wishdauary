package com.wishdiary.adapter.in.web.auth;

import com.wishdiary.application.auth.AuthTokenVerifierPort;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.domain.auth.MissingAuthTokenException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthenticatedSessionArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthTokenVerifierPort authTokenVerifierPort;

    public AuthenticatedSessionArgumentResolver(AuthTokenVerifierPort authTokenVerifierPort) {
        this.authTokenVerifierPort = authTokenVerifierPort;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Authenticated.class)
                && AuthenticatedSession.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            org.springframework.web.bind.support.WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new MissingAuthTokenException();
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new MissingAuthTokenException();
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new MissingAuthTokenException();
        }
        return authTokenVerifierPort.verify(token);
    }
}
