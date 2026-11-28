package com.ashish.resultswebsite.market;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MarketResultResponseTests {

    @Test
    void formatsResultWithoutDroppingLeadingZeroes() {
        MarketResult result = new MarketResult();
        result.setMarketName("SITA MORNING");
        result.setResultDate(LocalDate.parse("2026-06-30"));
        result.setOpenPana("007");
        result.setOpenDigit("0");
        result.setClosePana("006");
        result.setCloseDigit("3");
        result.setUpdatedAt(Instant.parse("2026-06-30T12:00:00Z"));

        MarketResultResponse response = MarketResultResponse.from(result);

        assertEquals("03", response.jodi());
        assertEquals("007-03-006", response.formattedResult());
    }

    @Test
    void formatsOpenStageBeforeCloseValuesArrive() {
        MarketResult result = new MarketResult();
        result.setMarketName("SITA MORNING");
        result.setResultDate(LocalDate.parse("2026-06-30"));
        result.setOpenPana("260");
        result.setOpenDigit("8");
        result.setUpdatedAt(Instant.parse("2026-06-30T12:00:00Z"));

        MarketResultResponse response = MarketResultResponse.from(result);

        assertEquals("260-8", response.formattedResult());
        assertNull(response.jodi());
    }
}
