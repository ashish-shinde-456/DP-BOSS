package com.ashish.resultswebsite.market;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record MarketResultWebhook(
        @NotBlank String marketName,
        @NotNull LocalDate resultDate,
        @Pattern(regexp = "\\d{3}") String apiOpenPana,
        @Pattern(regexp = "\\d") String apiOpenDigit,
        @Pattern(regexp = "\\d{3}") String apiClosePana,
        @Pattern(regexp = "\\d") String apiCloseDigit
) {

    public MarketResultWebhook {
        apiOpenPana = normalizeOptional(apiOpenPana);
        apiOpenDigit = normalizeOptional(apiOpenDigit);
        apiClosePana = normalizeOptional(apiClosePana);
        apiCloseDigit = normalizeOptional(apiCloseDigit);
    }

    @AssertTrue(message = "Provide a complete open pair or a complete close pair.")
    public boolean isValidStage() {
        boolean hasOpenPana = apiOpenPana != null;
        boolean hasOpenDigit = apiOpenDigit != null;
        boolean hasClosePana = apiClosePana != null;
        boolean hasCloseDigit = apiCloseDigit != null;

        return hasOpenPana == hasOpenDigit
                && hasClosePana == hasCloseDigit
                && ((hasOpenPana && hasOpenDigit) || (hasClosePana && hasCloseDigit));
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
