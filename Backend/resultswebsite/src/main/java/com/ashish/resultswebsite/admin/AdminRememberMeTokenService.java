package com.ashish.resultswebsite.admin;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminRememberMeTokenService {

    public static final String COOKIE_NAME = "DPBOSS_REMEMBER";
    private static final Duration TOKEN_VALIDITY = Duration.ofDays(30);
    private static final String COOKIE_PATH = "/api";

    private final AdminRememberMeTokenRepository tokenRepository;
    private final AdminUserRepository userRepository;
    private final AdminUserDetailsService userDetailsService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AdminRememberMeTokenService(
            AdminRememberMeTokenRepository tokenRepository,
            AdminUserRepository userRepository,
            AdminUserDetailsService userDetailsService
    ) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public void replaceRememberedLogin(
            String phoneNumber,
            boolean rememberMe,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        deleteCurrentToken(request, response);
        clearCookie(response);
        if (!rememberMe) return;

        Instant now = Instant.now();
        String rawToken = createRawToken();
        AdminRememberMeToken token = new AdminRememberMeToken();
        token.setTokenId(UUID.randomUUID().toString());
        token.setPhoneNumber(phoneNumber);
        token.setTokenHash(hashToken(rawToken));
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(TOKEN_VALIDITY));
        tokenRepository.save(token);

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                createCookie(token.getTokenId() + "." + rawToken, TOKEN_VALIDITY).toString()
        );
    }

    @Transactional
    public void revokeCurrentToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        deleteCurrentToken(request, response);
        clearCookie(response);
    }

    @Transactional
    public Optional<UserDetails> authenticateCookie(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Optional<TokenParts> parts = readTokenParts(request, response);
        if (parts.isEmpty()) return Optional.empty();

        Optional<AdminRememberMeToken> storedToken =
                tokenRepository.findById(parts.get().tokenId());
        if (storedToken.isEmpty()) {
            clearCookie(response);
            return Optional.empty();
        }

        AdminRememberMeToken token = storedToken.get();
        Instant now = Instant.now();
        boolean validToken = token.getExpiresAt().isAfter(now)
                && matchesHash(parts.get().rawToken(), token.getTokenHash());
        Optional<AdminUser> activeUser = userRepository.findById(token.getPhoneNumber())
                .filter(AdminUser::isActive);
        boolean validAccount = activeUser
                .map(user -> !token.getCreatedAt().isBefore(user.getPasswordChangedAt()))
                .orElse(false);

        if (!validToken || !validAccount) {
            tokenRepository.delete(token);
            clearCookie(response);
            return Optional.empty();
        }

        try {
            return Optional.of(userDetailsService.loadUserByUsername(token.getPhoneNumber()));
        } catch (UsernameNotFoundException exception) {
            tokenRepository.delete(token);
            clearCookie(response);
            return Optional.empty();
        }
    }

    private void deleteCurrentToken(HttpServletRequest request, HttpServletResponse response) {
        readTokenParts(request, response).ifPresent(parts ->
                tokenRepository.findById(parts.tokenId())
                        .filter(token -> matchesHash(parts.rawToken(), token.getTokenHash()))
                        .ifPresent(tokenRepository::delete)
        );
    }

    private Optional<TokenParts> readTokenParts(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (request.getCookies() == null) return Optional.empty();

        for (Cookie cookie : request.getCookies()) {
            if (!COOKIE_NAME.equals(cookie.getName())) continue;

            String value = cookie.getValue();
            if (value == null) {
                clearCookie(response);
                return Optional.empty();
            }
            int separator = value.indexOf('.');
            if (separator <= 0 || separator == value.length() - 1) {
                clearCookie(response);
                return Optional.empty();
            }

            String tokenId = value.substring(0, separator);
            String rawToken = value.substring(separator + 1);
            try {
                UUID.fromString(tokenId);
                if (rawToken.length() != 43) {
                    clearCookie(response);
                    return Optional.empty();
                }
                return Optional.of(new TokenParts(tokenId, rawToken));
            } catch (IllegalArgumentException exception) {
                clearCookie(response);
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private String createRawToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String hashToken(String rawToken) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }

    private boolean matchesHash(String rawToken, String expectedHash) {
        byte[] actual = hashToken(rawToken).getBytes(StandardCharsets.US_ASCII);
        byte[] expected = expectedHash.getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(actual, expected);
    }

    private void clearCookie(HttpServletResponse response) {
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                createCookie("", Duration.ZERO).toString()
        );
    }

    private ResponseCookie createCookie(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path(COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }

    private record TokenParts(String tokenId, String rawToken) {
    }
}
