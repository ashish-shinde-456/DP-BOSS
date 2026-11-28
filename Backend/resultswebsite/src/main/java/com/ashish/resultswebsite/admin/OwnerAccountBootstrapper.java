package com.ashish.resultswebsite.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class OwnerAccountBootstrapper implements ApplicationRunner {

    private final AdminUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final List<OwnerSeed> owners;

    public OwnerAccountBootstrapper(
            AdminUserRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-owner.nitin-phone}") String nitinPhone,
            @Value("${app.bootstrap-owner.nitin-name}") String nitinName,
            @Value("${app.bootstrap-owner.nitin-password:}") String nitinPassword,
            @Value("${app.bootstrap-owner.dayanand-phone}") String dayanandPhone,
            @Value("${app.bootstrap-owner.dayanand-name}") String dayanandName,
            @Value("${app.bootstrap-owner.dayanand-password:}") String dayanandPassword,
            @Value("${app.bootstrap-owner.ashish-phone}") String ashishPhone,
            @Value("${app.bootstrap-owner.ashish-name}") String ashishName,
            @Value("${app.bootstrap-owner.ashish-password:}") String ashishPassword
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.owners = List.of(
                new OwnerSeed(nitinPhone, nitinName, nitinPassword, "OWNER_NITIN_PASSWORD"),
                new OwnerSeed(dayanandPhone, dayanandName, dayanandPassword, "OWNER_DAYANAND_PASSWORD"),
                new OwnerSeed(ashishPhone, ashishName, ashishPassword, "OWNER_ASHISH_PASSWORD")
        );
    }

    @Override
    public void run(ApplicationArguments args) {
        List<OwnerSeed> missingOwners = owners.stream()
                .filter(owner -> repository.findById(owner.phoneNumber()).isEmpty())
                .toList();

        for (OwnerSeed owner : owners) {
            repository.findById(owner.phoneNumber()).ifPresent(existing -> {
                if (existing.getRole() != AdminRole.OWNER || !existing.isActive()) {
                    throw new IllegalStateException(
                            "A protected owner account is inactive or has an invalid role."
                    );
                }
            });
        }

        for (OwnerSeed owner : missingOwners) {
            validateOwnerSeed(owner);
        }

        for (OwnerSeed owner : missingOwners) {
            AdminUser adminUser = new AdminUser();
            Instant now = Instant.now();
            adminUser.setPhoneNumber(owner.phoneNumber());
            adminUser.setDisplayName(owner.displayName());
            adminUser.setPasswordHash(passwordEncoder.encode(owner.password()));
            adminUser.setRole(AdminRole.OWNER);
            adminUser.setActive(true);
            adminUser.setCreatedAt(now);
            adminUser.setPasswordChangedAt(now);
            repository.save(adminUser);
        }
    }

    private void validateOwnerSeed(OwnerSeed owner) {
        if (owner.phoneNumber() == null || !owner.phoneNumber().matches("\\d{10}")) {
            throw new IllegalStateException("Owner phone numbers must contain exactly 10 digits.");
        }
        if (owner.displayName() == null || owner.displayName().isBlank()) {
            throw new IllegalStateException("Owner names must be configured.");
        }
        if (!AdminPasswordPolicy.isValid(owner.password())) {
            throw new IllegalStateException(
                    "Set " + owner.passwordVariable()
                            + " to a password that meets the admin password policy."
            );
        }
    }

    private record OwnerSeed(
            String phoneNumber,
            String displayName,
            String password,
            String passwordVariable
    ) {
    }
}
