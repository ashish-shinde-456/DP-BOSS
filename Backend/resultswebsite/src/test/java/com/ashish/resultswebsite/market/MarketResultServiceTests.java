package com.ashish.resultswebsite.market;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MarketResultServiceTests {

    @Test
    void mergesCloseWebhookIntoPreviouslySavedOpenResult() {
        MarketResultRepository repository = mock(MarketResultRepository.class);
        AtomicReference<MarketResult> storedResult = new AtomicReference<>();
        LocalDate resultDate = LocalDate.parse("2026-06-30");

        when(repository.findByMarketNameIgnoreCaseAndResultDate(
                eq("SITA MORNING"),
                eq(resultDate)
        )).thenAnswer(invocation -> Optional.ofNullable(storedResult.get()));
        when(repository.save(any(MarketResult.class))).thenAnswer(invocation -> {
            MarketResult result = invocation.getArgument(0);
            storedResult.set(result);
            return result;
        });

        MarketResultService service = new MarketResultService(repository);
        MarketResultResponse openResponse = service.upsert(new MarketResultWebhook(
                "SITA MORNING", resultDate, "260", "8", null, null
        ));
        MarketResultResponse completeResponse = service.upsert(new MarketResultWebhook(
                "SITA MORNING", resultDate, null, null, "468", "4"
        ));

        assertEquals("260-8", openResponse.formattedResult());
        assertEquals("260-84-468", completeResponse.formattedResult());
        assertEquals("260", storedResult.get().getOpenPana());
        assertEquals("8", storedResult.get().getOpenDigit());
    }

    @Test
    void keepsPreviousResultUntilTheNextOpenPairArrives() {
        MarketResultRepository repository = mock(MarketResultRepository.class);
        LocalDate previousDate = LocalDate.parse("2026-06-30");
        MarketResult previousResult = createResult(
                previousDate,
                "260",
                "8",
                "468",
                "4"
        );
        MarketResult closeOnlyResult = createResult(
                previousDate.plusDays(1),
                null,
                null,
                "111",
                "1"
        );
        AtomicReference<List<MarketResult>> storedResults =
                new AtomicReference<>(List.of(closeOnlyResult, previousResult));
        when(repository.findAllByOrderByResultDateDescUpdatedAtDesc())
                .thenAnswer(invocation -> storedResults.get());

        MarketResultService service = new MarketResultService(repository);

        MarketResultResponse previousResponse = service.getLatestPerMarket().getFirst();
        assertEquals(previousDate, previousResponse.resultDate());
        assertEquals("260-84-468", previousResponse.formattedResult());

        MarketResult nextOpenResult = createResult(
                previousDate.plusDays(1),
                "223",
                "7",
                null,
                null
        );
        storedResults.set(List.of(nextOpenResult, closeOnlyResult, previousResult));

        MarketResultResponse nextOpenResponse = service.getLatestPerMarket().getFirst();
        assertEquals(previousDate.plusDays(1), nextOpenResponse.resultDate());
        assertEquals("223-7", nextOpenResponse.formattedResult());
        assertNull(nextOpenResponse.jodi());
        assertNull(nextOpenResponse.apiClosePana());
    }

    private MarketResult createResult(
            LocalDate resultDate,
            String openPana,
            String openDigit,
            String closePana,
            String closeDigit
    ) {
        MarketResult result = new MarketResult();
        result.setMarketName("SITA MORNING");
        result.setResultDate(resultDate);
        result.setOpenPana(openPana);
        result.setOpenDigit(openDigit);
        result.setClosePana(closePana);
        result.setCloseDigit(closeDigit);
        result.setUpdatedAt(Instant.now());
        return result;
    }
}
