package com.ashish.resultswebsite.config;

import com.ashish.resultswebsite.admin.AdminUserDetailsService;
import com.ashish.resultswebsite.admin.AdminRememberMeTokenService;
import com.ashish.resultswebsite.admin.RememberMeAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            AdminUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider,
            AdminRememberMeTokenService rememberMeTokenService
    ) throws Exception {
        return http
                .authenticationProvider(authenticationProvider)
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/admin/session", "/api/admin/session/**").permitAll()
                        .requestMatchers("/api/admin/market-timings")
                        .hasRole("OWNER")
                        .requestMatchers("/api/admin/me", "/api/admin/password")
                        .hasAnyRole("OWNER", "EDITOR", "PASSWORD_EXPIRED")
                        .requestMatchers(
                                "/api/admin/users",
                                "/api/admin/users/**",
                                "/api/admin/audit",
                                "/api/admin/audit/**"
                        ).hasRole("OWNER")
                        .requestMatchers("/api/admin/**").hasAnyRole("OWNER", "EDITOR")
                        .anyRequest().permitAll())
                .httpBasic(basic -> basic
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(
                        new RememberMeAuthenticationFilter(rememberMeTokenService),
                        BasicAuthenticationFilter.class
                )
                .build();
    }
}
