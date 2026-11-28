package com.ashish.resultswebsite.admin;

public record ChangeAdminPasswordRequest(
        String newPassword
) {

    @Override
    public String toString() {
        return "ChangeAdminPasswordRequest[newPassword=[REDACTED]]";
    }
}
