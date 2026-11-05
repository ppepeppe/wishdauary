package com.wishdiary.adapter.out.security;

import com.wishdiary.application.auth.AuthTokenIssueRequest;
import com.wishdiary.application.auth.AuthTokenIssuerPort;
import com.wishdiary.application.auth.AuthTokenVerifierPort;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.domain.auth.InvalidAuthTokenException;
import com.wishdiary.domain.user.AppRole;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HmacAuthTokenAdapter implements AuthTokenIssuerPort, AuthTokenVerifierPort {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] secret;
    private final long expirationSeconds;
    private final Clock clock;

    public HmacAuthTokenAdapter(
            @Value("${wishdiary.auth.token-secret:local-development-token-secret-change-me}") String secret,
            @Value("${wishdiary.auth.token-expiration-seconds:86400}") long expirationSeconds
    ) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
        this.clock = Clock.systemUTC();
    }

    @Override
    public String issue(AuthTokenIssueRequest request) {
        long expiresAt = Instant.now(clock).plusSeconds(expirationSeconds).getEpochSecond();
        String payload = "userId=" + request.userId()
                + "&groupId=" + request.groupId()
                + "&appRole=" + request.appRole()
                + "&exp=" + expiresAt;
        String encodedPayload = encode(payload.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + "." + sign(encodedPayload);
    }

    @Override
    public AuthenticatedSession verify(String token) {
        String[] parts = token == null ? new String[0] : token.split("\\.", -1);
        if (parts.length != 2 || !sign(parts[0]).equals(parts[1])) {
            throw new InvalidAuthTokenException();
        }

        Map<String, String> claims = parsePayload(parts[0]);
        long expiresAt = parseLong(claims.get("exp"));
        if (expiresAt < Instant.now(clock).getEpochSecond()) {
            throw new InvalidAuthTokenException();
        }

        return new AuthenticatedSession(
                parseLong(claims.get("userId")),
                parseLong(claims.get("groupId")),
                parseAppRole(required(claims, "appRole"))
        );
    }

    private AppRole parseAppRole(String value) {
        try {
            return AppRole.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidAuthTokenException();
        }
    }

    private Map<String, String> parsePayload(String encodedPayload) {
        String payload;
        try {
            payload = new String(Base64.getUrlDecoder().decode(encodedPayload), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw new InvalidAuthTokenException();
        }

        return Arrays.stream(payload.split("&"))
                .map(part -> part.split("=", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1], (left, right) -> right, LinkedHashMap::new));
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (RuntimeException exception) {
            throw new InvalidAuthTokenException();
        }
    }

    private String required(Map<String, String> claims, String key) {
        String value = claims.get(key);
        if (value == null || value.isBlank()) {
            throw new InvalidAuthTokenException();
        }
        return value;
    }

    private String sign(String encodedPayload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return encode(mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign authentication token.", exception);
        }
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
