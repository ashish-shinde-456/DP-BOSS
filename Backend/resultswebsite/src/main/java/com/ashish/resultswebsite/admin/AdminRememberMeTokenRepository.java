package com.ashish.resultswebsite.admin;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRememberMeTokenRepository
        extends JpaRepository<AdminRememberMeToken, String> {

    void deleteAllByPhoneNumber(String phoneNumber);
}
