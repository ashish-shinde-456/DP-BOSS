package com.ashish.resultswebsite.market;

import java.time.Instant;
import java.time.LocalDate;

public record MarketResultResponse(
        String marketName,
        LocalDate resultDate,
        String apiOpenPana,
        String apiOpenDigit,
        String jodi,
        String apiClosePana,
        String apiCloseDigit,
        String formattedResult,
        Instant updatedAt
) {

    public static MarketResultResponse from(MarketResult result) {
        String jodi = null;
        String formattedResult = null;
        boolean hasOpenResult = result.getOpenPana() != null && result.getOpenDigit() != null;
        boolean hasCloseResult = result.getClosePana() != null && result.getCloseDigit() != null;
        if (hasOpenResult && hasCloseResult) {
            jodi = result.getOpenDigit() + result.getCloseDigit();
            formattedResult = result.getOpenPana() + "-" + jodi + "-" + result.getClosePana();
        } else if (hasOpenResult) {
            formattedResult = result.getOpenPana() + "-" + result.getOpenDigit();
        }

        return new MarketResultResponse(
                result.getMarketName(),
                result.getResultDate(),
                result.getOpenPana(),
                result.getOpenDigit(),
                jodi,
                result.getClosePana(),
                result.getCloseDigit(),
                formattedResult,
                result.getUpdatedAt()
        );
    }
}
