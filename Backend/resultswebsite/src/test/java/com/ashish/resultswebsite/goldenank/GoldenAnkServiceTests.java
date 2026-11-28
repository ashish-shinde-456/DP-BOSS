package com.ashish.resultswebsite.goldenank;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GoldenAnkServiceTests {

    @Test
    void currentGoldenAnkUsesTheMostRecentEffectiveDate() {
        GoldenAnkResultRepository repository = mock(GoldenAnkResultRepository.class);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        GoldenAnkResult result = new GoldenAnkResult();
        result.setResultDate(today.minusDays(1));
        result.setAnkOne("1");
        result.setAnkTwo("6");
        result.setAnkThree("0");
        result.setAnkFour("5");

        when(repository.findFirstByResultDateLessThanEqualOrderByResultDateDesc(today))
                .thenReturn(Optional.of(result));

        GoldenAnkResponse response = new GoldenAnkService(repository)
                .getCurrent()
                .orElseThrow();

        assertEquals(today.minusDays(1), response.resultDate());
        assertEquals("1-6-0-5", response.goldenAnk());
    }

    @Test
    void savesAResultForAFutureEffectiveDate() {
        GoldenAnkResultRepository repository = mock(GoldenAnkResultRepository.class);
        LocalDate tomorrow = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(1);
        when(repository.findByResultDate(tomorrow)).thenReturn(Optional.empty());
        when(repository.save(any(GoldenAnkResult.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        GoldenAnkResponse response = new GoldenAnkService(repository).save(
                new GoldenAnkRequest(tomorrow, "1", "6", "0", "5")
        );

        assertEquals(tomorrow, response.resultDate());
        assertEquals("1-6-0-5", response.goldenAnk());
    }

    @Test
    void rejectsPastEffectiveDates() {
        GoldenAnkService service = new GoldenAnkService(mock(GoldenAnkResultRepository.class));
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Kolkata")).minusDays(1);

        assertThrows(
                ResponseStatusException.class,
                () -> service.save(new GoldenAnkRequest(yesterday, "1", "6", "0", "5"))
        );
    }
}
