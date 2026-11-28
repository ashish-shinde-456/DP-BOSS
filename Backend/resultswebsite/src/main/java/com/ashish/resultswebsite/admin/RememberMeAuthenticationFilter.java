package com.ashish.resultswebsite.admin;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public class RememberMeAuthenticationFilter extends OncePerRequestFilter {

    private static final String REQUEST_HEADER = "X-Requested-With";
    private static final String REQUEST_HEADER_VALUE = "XMLHttpRequest";
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private final AdminRememberMeTokenService tokenService;

    public RememberMeAuthenticationFilter(AdminRememberMeTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<UserDetails> userDetails = tokenService.authenticateCookie(request, response);
            if (userDetails.isPresent()) {
                if (!SAFE_METHODS.contains(request.getMethod())
                        && !REQUEST_HEADER_VALUE.equals(request.getHeader(REQUEST_HEADER))) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                UserDetails user = userDetails.get();
                UsernamePasswordAuthenticationToken authentication =
                        UsernamePasswordAuthenticationToken.authenticated(
                                user,
                                null,
                                user.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
