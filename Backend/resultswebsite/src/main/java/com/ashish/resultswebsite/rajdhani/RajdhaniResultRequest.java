package com.ashish.resultswebsite.rajdhani;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDate;

public record RajdhaniResultRequest(
        @NotNull LocalDate resultDate,
        @Pattern(regexp = "\\d{3}") String openPana,
        @Pattern(regexp = "\\d") String openDigit,
        @Pattern(regexp = "\\d{3}") String closePana,
        @Pattern(regexp = "\\d") String closeDigit
) {

    @AssertTrue(message = "Provide both open fields or both close fields.")
    public boolean isValidStage() {
        boolean hasOpenPana = openPana != null;
        boolean hasOpenDigit = openDigit != null;
        boolean hasClosePana = closePana != null;
        boolean hasCloseDigit = closeDigit != null;
        boolean completeOpenPair = hasOpenPana && hasOpenDigit;
        boolean completeClosePair = hasClosePana && hasCloseDigit;

        return hasOpenPana == hasOpenDigit
                && hasClosePana == hasCloseDigit
                && (completeOpenPair || completeClosePair);
    }

    public boolean hasOpenResult() {
        return openPana != null && openDigit != null;
    }

    public boolean hasCloseResult() {
        return closePana != null && closeDigit != null;
    }
}
