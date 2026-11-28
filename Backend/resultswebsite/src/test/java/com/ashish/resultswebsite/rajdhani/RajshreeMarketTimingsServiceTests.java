package com.ashish.resultswebsite.rajdhani;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RajshreeMarketTimingsServiceTests {

    @Test
    void returnsCurrentDefaultsUntilAnOwnerChangesThem() {
        RajshreeMarketTimingsService service = new RajshreeMarketTimingsService(
                mock(RajshreeMarketTimingRepository.class),
                mock(ScheduledRajdhaniResultRepository.class)
        );

        RajshreeMarketTimingsResponse response = service.getCurrent();

        assertEquals("15:15", response.dayOpenTime());
        assertEquals("17:15", response.dayCloseTime());
        assertEquals("21:35", response.nightOpenTime());
        assertEquals("23:45", response.nightCloseTime());
    }

    @Test
    void savesTimingsAndReschedulesUnpublishedStagesIncludingOvernightClose() {
        RajshreeMarketTimingRepository timingRepository =
                mock(RajshreeMarketTimingRepository.class);
        ScheduledRajdhaniResultRepository resultRepository =
                mock(ScheduledRajdhaniResultRepository.class);
        LocalDate resultDate = LocalDate.parse("2026-11-28");
        ScheduledRajdhaniResult dayResult = pendingResult(RajdhaniMarket.RAJDHAN_DAY, resultDate);
        ScheduledRajdhaniResult nightResult = pendingResult(RajdhaniMarket.RAJDHAN_NIGHT, resultDate);
        Map<RajdhaniMarket, RajshreeMarketTiming> savedTimings =
                new EnumMap<>(RajdhaniMarket.class);

        when(timingRepository.findById(any())).thenAnswer(invocation ->
                Optional.ofNullable(savedTimings.get(invocation.getArgument(0))));
        when(timingRepository.save(any(RajshreeMarketTiming.class))).thenAnswer(invocation -> {
            RajshreeMarketTiming timing = invocation.getArgument(0);
            savedTimings.put(timing.getMarket(), timing);
            return timing;
        });
        when(resultRepository.findAllByMarketAndOpenPublishedFalse(RajdhaniMarket.RAJDHAN_DAY))
                .thenReturn(List.of(dayResult));
        when(resultRepository.findAllByMarketAndPublishedFalse(RajdhaniMarket.RAJDHAN_DAY))
                .thenReturn(List.of(dayResult));
        when(resultRepository.findAllByMarketAndOpenPublishedFalse(RajdhaniMarket.RAJDHAN_NIGHT))
                .thenReturn(List.of(nightResult));
        when(resultRepository.findAllByMarketAndPublishedFalse(RajdhaniMarket.RAJDHAN_NIGHT))
                .thenReturn(List.of(nightResult));
        RajshreeMarketTimingsService service = new RajshreeMarketTimingsService(
                timingRepository,
                resultRepository
        );
        RajshreeMarketTimingsResponse response = service.save(
                new RajshreeMarketTimingsRequest(
                        LocalTime.of(16, 0),
                        LocalTime.of(18, 0),
                        LocalTime.of(22, 0),
                        LocalTime.of(0, 15)
                ),
                "1234567890"
        );

        ZoneId indiaZone = ZoneId.of("Asia/Kolkata");
        assertEquals("16:00", response.dayOpenTime());
        assertEquals("18:00", response.dayCloseTime());
        RajshreeMarketTimingsResponse reloadedResponse = service.getCurrent();
        assertEquals("16:00", reloadedResponse.dayOpenTime());
        assertEquals("00:15", reloadedResponse.nightCloseTime());
        assertEquals(
                resultDate.atTime(16, 0).atZone(indiaZone).toInstant(),
                dayResult.getOpenPublishAt()
        );
        assertEquals(
                resultDate.atTime(18, 0).atZone(indiaZone).toInstant(),
                dayResult.getPublishAt()
        );
        assertEquals(
                resultDate.atTime(22, 0).atZone(indiaZone).toInstant(),
                nightResult.getOpenPublishAt()
        );
        assertEquals(
                resultDate.plusDays(1).atTime(0, 15).atZone(indiaZone).toInstant(),
                nightResult.getPublishAt()
        );
        ArgumentCaptor<RajshreeMarketTiming> timingCaptor =
                ArgumentCaptor.forClass(RajshreeMarketTiming.class);
        verify(timingRepository, org.mockito.Mockito.times(2)).save(timingCaptor.capture());
        assertEquals(
                "1234567890",
                timingCaptor.getAllValues().get(1).getUpdatedByPhone()
        );
    }

    @Test
    void rejectsIdenticalOpenAndCloseTimes() {
        RajshreeMarketTimingsService service = new RajshreeMarketTimingsService(
                mock(RajshreeMarketTimingRepository.class),
                mock(ScheduledRajdhaniResultRepository.class)
        );

        assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> service.save(
                        new RajshreeMarketTimingsRequest(
                                LocalTime.of(15, 0),
                                LocalTime.of(15, 0),
                                LocalTime.of(21, 0),
                                LocalTime.of(23, 0)
                        ),
                        "1234567890"
                )
        );
    }

    private ScheduledRajdhaniResult pendingResult(RajdhaniMarket market, LocalDate resultDate) {
        ScheduledRajdhaniResult result = new ScheduledRajdhaniResult();
        result.setMarket(market);
        result.setResultDate(resultDate);
        result.setOpenPana("260");
        result.setOpenDigit("8");
        result.setOpenPublishAt(Instant.EPOCH);
        result.setPublishAt(Instant.EPOCH);
        return result;
    }
}
