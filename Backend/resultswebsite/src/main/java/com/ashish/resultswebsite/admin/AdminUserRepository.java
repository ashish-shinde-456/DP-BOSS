package com.ashish.resultswebsite.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminUserRepository extends JpaRepository<AdminUser, String> {

    List<AdminUser> findAllByOrderByDisplayNameAsc();
}
