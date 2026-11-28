package com.ashish.resultswebsite.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminSessionRequest(
        @NotBlank String phoneNumber,
        @NotBlank String password,
        boolean rememberMe
) {
}
