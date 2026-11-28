package com.ashish.resultswebsite.admin;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository repository;

    public AdminUserDetailsService(AdminUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String phoneNumber) {
        AdminUser adminUser = repository.findById(phoneNumber)
                .filter(AdminUser::isActive)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid phone number or password."));

        String role = adminUser.isPasswordExpired(Instant.now())
                ? "PASSWORD_EXPIRED"
                : adminUser.getRole().name();

        return User.withUsername(adminUser.getPhoneNumber())
                .password(adminUser.getPasswordHash())
                .roles(role)
                .build();
    }
}
