package com.ashish.resultswebsite.admin;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class AdminUserService {

    private final AdminUserRepository repository;
    private final AdminRememberMeTokenRepository rememberMeTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(
            AdminUserRepository repository,
            AdminRememberMeTokenRepository rememberMeTokenRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.repository = repository;
        this.rememberMeTokenRepository = rememberMeTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getUsers() {
        return repository.findAllByOrderByDisplayNameAsc()
                .stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    @Transactional
    public AdminUserResponse createEditor(AdminUserRequest request, String ownerPhone) {
        if (!AdminPasswordPolicy.isValid(request.password())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    AdminPasswordPolicy.VALIDATION_MESSAGE
            );
        }

        AdminUser user = repository.findById(request.phoneNumber()).orElse(null);
        if (user != null && user.getRole() == AdminRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That phone number belongs to an owner.");
        }
        if (user != null && user.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That phone number already has an active account.");
        }
        if (user == null) {
            user = new AdminUser();
            user.setPhoneNumber(request.phoneNumber());
            user.setCreatedAt(Instant.now());
            user.setCreatedByPhone(ownerPhone);
        }

        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(AdminRole.EDITOR);
        user.setActive(true);
        user.setPasswordChangedAt(Instant.now());
        rememberMeTokenRepository.deleteAllByPhoneNumber(user.getPhoneNumber());

        return AdminUserResponse.from(repository.save(user));
    }

    @Transactional
    public AdminUserResponse changePassword(String phoneNumber, ChangeAdminPasswordRequest request) {
        if (!AdminPasswordPolicy.isValid(request.newPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    AdminPasswordPolicy.VALIDATION_MESSAGE
            );
        }

        AdminUser user = repository.findById(phoneNumber)
                .filter(AdminUser::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "This admin account is no longer active."
                ));
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Choose a password different from your current password."
            );
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(Instant.now());
        rememberMeTokenRepository.deleteAllByPhoneNumber(phoneNumber);
        return AdminUserResponse.from(repository.save(user));
    }

    @Transactional
    public AdminUserResponse deactivateEditor(String phoneNumber) {
        AdminUser user = repository.findById(phoneNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found."));
        if (user.getRole() == AdminRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Owner accounts cannot be removed.");
        }

        user.setActive(false);
        rememberMeTokenRepository.deleteAllByPhoneNumber(phoneNumber);
        return AdminUserResponse.from(repository.save(user));
    }
}
