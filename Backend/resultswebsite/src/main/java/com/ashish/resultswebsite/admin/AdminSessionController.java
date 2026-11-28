package com.ashish.resultswebsite.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/session")
public class AdminSessionController {

    private final AuthenticationManager authenticationManager;
    private final AdminRememberMeTokenService tokenService;

    public AdminSessionController(
            AuthenticationManager authenticationManager,
            AdminRememberMeTokenService tokenService
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void login(
            @Valid @RequestBody AdminSessionRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.phoneNumber(),
                    request.password()
            ));
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid mobile number or password."
            );
        }

        tokenService.replaceRememberedLogin(
                request.phoneNumber(),
                request.rememberMe(),
                servletRequest,
                servletResponse
        );
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        tokenService.revokeCurrentToken(servletRequest, servletResponse);
    }
}
