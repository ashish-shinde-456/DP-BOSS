package com.ashish.resultswebsite.rajdhani;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record RajshreeMarketTimingsRequest(
        @NotNull LocalTime dayOpenTime,
        @NotNull LocalTime dayCloseTime,
        @NotNull LocalTime nightOpenTime,
        @NotNull LocalTime nightCloseTime
) {

    @AssertTrue(message = "Each market's open and close times must be different.")
    public boolean hasDifferentOpenAndCloseTimes() {
        return dayOpenTime == null
                || dayCloseTime == null
                || nightOpenTime == null
                || nightCloseTime == null
                || (!dayOpenTime.equals(dayCloseTime)
                && !nightOpenTime.equals(nightCloseTime));
    }
}
