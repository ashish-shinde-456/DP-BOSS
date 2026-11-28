package com.ashish.resultswebsite.rajdhani;

import java.time.Instant;
import java.time.LocalDate;

public record RajdhaniResultResponse(
        String marketName,
        LocalDate resultDate,
        String openPana,
        String openDigit,
        String jodi,
        String closePana,
        String closeDigit,
        String formattedResult,
        Instant openPublishAt,
        Instant publishAt,
        boolean openPublished,
        boolean published,
        Instant openPublishedAt,
        Instant publishedAt,
        Instant updatedAt
) {

    public static RajdhaniResultResponse from(ScheduledRajdhaniResult result) {
        String jodi = null;
        String formattedResult = null;
        if (result.getOpenPana() != null && result.getOpenDigit() != null) {
            if (result.getClosePana() != null && result.getCloseDigit() != null) {
                jodi = result.getOpenDigit() + result.getCloseDigit();
                formattedResult = result.getOpenPana() + "-" + jodi + "-" + result.getClosePana();
            } else {
                formattedResult = result.getOpenPana() + "-" + result.getOpenDigit();
            }
        }

        return new RajdhaniResultResponse(
                result.getMarket().getDisplayName(),
                result.getResultDate(),
                result.getOpenPana(),
                result.getOpenDigit(),
                jodi,
                result.getClosePana(),
                result.getCloseDigit(),
                formattedResult,
                result.getOpenPublishAt(),
                result.getPublishAt(),
                result.isOpenPublished(),
                result.isPublished(),
                result.getOpenPublishedAt(),
                result.getPublishedAt(),
                result.getUpdatedAt()
        );
    }
}
