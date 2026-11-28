package com.ashish.resultswebsite.rajdhani;

import java.time.format.DateTimeFormatter;

public record RajshreeMarketTimingsResponse(
        String dayOpenTime,
        String dayCloseTime,
        String nightOpenTime,
        String nightCloseTime
) {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public static RajshreeMarketTimingsResponse from(
            RajshreeMarketTiming day,
            RajshreeMarketTiming night
    ) {
        return new RajshreeMarketTimingsResponse(
                TIME_FORMAT.format(day.getOpenTime()),
                TIME_FORMAT.format(day.getCloseTime()),
                TIME_FORMAT.format(night.getOpenTime()),
                TIME_FORMAT.format(night.getCloseTime())
        );
    }
}
