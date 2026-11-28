package com.ashish.resultswebsite.rajdhani;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RajdhaniResultResponseTests {

    @Test
    void usesNewMarketNameAndPreservesLeadingZeroes() {
        ScheduledRajdhaniResult result = new ScheduledRajdhaniResult();
        result.setMarket(RajdhaniMarket.RAJDHAN_DAY);
        result.setResultDate(LocalDate.parse("2026-11-22"));
        result.setOpenPana("007");
        result.setOpenDigit("0");
        result.setClosePana("006");
        result.setCloseDigit("3");
        result.setPublishAt(Instant.parse("2026-11-22T11:45:00Z"));
        result.setUpdatedAt(Instant.parse("2026-11-22T10:00:00Z"));

        RajdhaniResultResponse response = RajdhaniResultResponse.from(result);

        assertEquals("RAJSHREE DAY", response.marketName());
        assertEquals("03", response.jodi());
        assertEquals("007-03-006", response.formattedResult());
    }

    @Test
    void formatsOpenStageAndUsesConfiguredMarketTimes() {
        ScheduledRajdhaniResult result = new ScheduledRajdhaniResult();
        result.setMarket(RajdhaniMarket.RAJDHAN_DAY);
        result.setResultDate(LocalDate.parse("2026-11-22"));
        result.setOpenPana("260");
        result.setOpenDigit("8");
        result.setOpenPublishAt(Instant.parse("2026-11-22T09:45:00Z"));
        result.setPublishAt(Instant.parse("2026-11-22T11:45:00Z"));

        RajdhaniResultResponse response = RajdhaniResultResponse.from(result);

        assertEquals("260-8", response.formattedResult());
        assertEquals(LocalTime.of(15, 15), RajdhaniMarket.RAJDHAN_DAY.getOpenTime());
        assertEquals(LocalTime.of(17, 15), RajdhaniMarket.RAJDHAN_DAY.getCloseTime());
        assertEquals(LocalTime.of(21, 35), RajdhaniMarket.RAJDHAN_NIGHT.getOpenTime());
        assertEquals(LocalTime.of(23, 45), RajdhaniMarket.RAJDHAN_NIGHT.getCloseTime());
    }
}
