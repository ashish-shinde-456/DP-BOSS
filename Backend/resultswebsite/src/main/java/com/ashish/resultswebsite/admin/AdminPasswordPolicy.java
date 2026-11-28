package com.ashish.resultswebsite.admin;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.regex.Pattern;

public final class AdminPasswordPolicy {

    public static final String REGEX =
            "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{12,72}";
    public static final String VALIDATION_MESSAGE =
            "Use 12 to 72 characters (maximum 72 UTF-8 bytes) with an uppercase letter, a lowercase letter, a number, and a symbol.";

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(REGEX);

    private AdminPasswordPolicy() {
    }

    public static boolean isValid(String password) {
        return password != null
                && password.length() >= 12
                && password.length() <= 72
                && password.getBytes(StandardCharsets.UTF_8).length <= 72
                && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static Instant expiresAt(Instant passwordChangedAt) {
        return passwordChangedAt.atZone(ZoneOffset.UTC).plusMonths(3).toInstant();
    }

    public static boolean isExpired(Instant passwordChangedAt, Instant now) {
        return !now.isBefore(expiresAt(passwordChangedAt));
    }
}
