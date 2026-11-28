package com.ashish.resultswebsite.admin;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminRememberMeTokenServiceTests {

    @Test
    void storesOnlyTokenHashAndAuthenticatesTheCookie() throws NoSuchAlgorithmException {
        AdminRememberMeTokenRepository tokenRepository = mock(AdminRememberMeTokenRepository.class);
        AdminUserRepository userRepository = mock(AdminUserRepository.class);
        AdminUserDetailsService userDetailsService = mock(AdminUserDetailsService.class);
        AdminRememberMeTokenService service = new AdminRememberMeTokenService(
                tokenRepository,
                userRepository,
                userDetailsService
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(tokenRepository.save(any(AdminRememberMeToken.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        service.replaceRememberedLogin("1234567890", true, request, response);

        ArgumentCaptor<AdminRememberMeToken> storedTokenCaptor =
                ArgumentCaptor.forClass(AdminRememberMeToken.class);
        verify(tokenRepository).save(storedTokenCaptor.capture());
        AdminRememberMeToken storedToken = storedTokenCaptor.getValue();
        String cookieHeader = response.getHeaders("Set-Cookie").stream()
                .map(Object::toString)
                .filter(value -> value.startsWith(AdminRememberMeTokenService.COOKIE_NAME + "="))
                .filter(value -> !value.startsWith(AdminRememberMeTokenService.COOKIE_NAME + "=;"))
                .findFirst()
                .orElseThrow();
        String cookieValue = cookieHeader.substring(
                (AdminRememberMeTokenService.COOKIE_NAME + "=").length(),
                cookieHeader.indexOf(';')
        );
        String rawToken = cookieValue.substring(cookieValue.indexOf('.') + 1);
        String expectedHash = Base64.getUrlEncoder().withoutPadding().encodeToString(
                MessageDigest.getInstance("SHA-256")
                        .digest(rawToken.getBytes(StandardCharsets.UTF_8))
        );
        assertEquals(expectedHash, storedToken.getTokenHash());
        assertFalse(storedToken.getTokenHash().equals(rawToken));
        assertTrue(cookieHeader.contains("HttpOnly"));
        assertTrue(cookieHeader.contains("Secure"));
        assertTrue(cookieHeader.contains("SameSite=None"));
        assertTrue(cookieHeader.contains("Path=/api"));
        assertTrue(cookieHeader.contains("Max-Age=2592000"));

        AdminUser activeUser = new AdminUser();
        activeUser.setPhoneNumber("1234567890");
        activeUser.setActive(true);
        activeUser.setPasswordChangedAt(Instant.now().minusSeconds(1));
        UserDetails userDetails = User.withUsername("1234567890")
                .password("unused")
                .roles("OWNER")
                .build();
        when(tokenRepository.findById(storedToken.getTokenId())).thenReturn(Optional.of(storedToken));
        when(userRepository.findById("1234567890")).thenReturn(Optional.of(activeUser));
        when(userDetailsService.loadUserByUsername("1234567890")).thenReturn(userDetails);
        request.setCookies(new Cookie(AdminRememberMeTokenService.COOKIE_NAME, cookieValue));

        assertEquals(userDetails, service.authenticateCookie(request, response).orElseThrow());
        verify(tokenRepository, never()).delete(any(AdminRememberMeToken.class));
    }

    @Test
    void rejectsAndDeletesExpiredTokens() {
        AdminRememberMeTokenRepository tokenRepository = mock(AdminRememberMeTokenRepository.class);
        AdminUserRepository userRepository = mock(AdminUserRepository.class);
        AdminRememberMeTokenService service = new AdminRememberMeTokenService(
                tokenRepository,
                userRepository,
                mock(AdminUserDetailsService.class)
        );
        AdminRememberMeToken token = new AdminRememberMeToken();
        token.setTokenId("d8e1eb2d-3b55-4ab1-b604-245e5fcd2277");
        token.setPhoneNumber("1234567890");
        token.setTokenHash("not-a-valid-token-hash");
        token.setCreatedAt(Instant.now().minusSeconds(60));
        token.setExpiresAt(Instant.now().minusSeconds(1));
        when(tokenRepository.findById(token.getTokenId())).thenReturn(Optional.of(token));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(
                AdminRememberMeTokenService.COOKIE_NAME,
                token.getTokenId() + "." + "a".repeat(43)
        ));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(service.authenticateCookie(request, response).isEmpty());
        verify(tokenRepository).delete(token);
        assertTrue(response.getHeaders("Set-Cookie").stream()
                .map(Object::toString)
                .anyMatch(value -> value.contains("Max-Age=0")));
    }
}
