package com.ashish.resultswebsite.admin;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/password")
public class AdminPasswordController {

    private final AdminUserService service;

    public AdminPasswordController(AdminUserService service) {
        this.service = service;
    }

    @PostMapping
    public AdminUserResponse changePassword(
            @Valid @RequestBody ChangeAdminPasswordRequest request,
            Authentication authentication
    ) {
        return service.changePassword(authentication.getName(), request);
    }
}
