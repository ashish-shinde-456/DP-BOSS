package com.ashish.resultswebsite.admin;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminPasswordPolicyTests {

    @Test
    void requiresLengthAndAllCharacterCategories() {
        assertTrue(AdminPasswordPolicy.isValid("Ashish@9623!"));
        assertTrue(AdminPasswordPolicy.isValid("Abcdefgh1!" + "é".repeat(31)));
        assertFalse(AdminPasswordPolicy.isValid("Ashish@9623"));
        assertFalse(AdminPasswordPolicy.isValid("ashish@9623!"));
        assertFalse(AdminPasswordPolicy.isValid("ASHISH@9623!"));
        assertFalse(AdminPasswordPolicy.isValid("Ashish962345"));
        assertFalse(AdminPasswordPolicy.isValid("Abcdefgh1!" + "é".repeat(32)));
    }

    @Test
    void expiresAfterThreeCalendarMonths() {
        Instant changedAt = Instant.parse("2026-01-31T12:00:00Z");
        Instant expiresAt = Instant.parse("2026-04-30T12:00:00Z");

        assertEquals(expiresAt, AdminPasswordPolicy.expiresAt(changedAt));
        assertFalse(AdminPasswordPolicy.isExpired(changedAt, expiresAt.minusMillis(1)));
        assertTrue(AdminPasswordPolicy.isExpired(changedAt, expiresAt));
    }
}
