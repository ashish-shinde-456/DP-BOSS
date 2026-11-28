package com.ashish.resultswebsite.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminUserRequest(
        @NotBlank @Size(max = 80) String displayName,
        @NotBlank @Pattern(regexp = "\\d{10}") String phoneNumber,
        String password
) {

    @Override
    public String toString() {
        return "AdminUserRequest[displayName=" + displayName
                + ", phoneNumber=" + phoneNumber
                + ", password=[REDACTED]]";
    }
}
