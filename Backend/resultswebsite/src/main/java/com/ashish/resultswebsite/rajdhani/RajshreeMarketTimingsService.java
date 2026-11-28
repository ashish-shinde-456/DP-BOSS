package com.ashish.resultswebsite.rajdhani;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class RajshreeMarketTimingsService {

    private static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");

    private final RajshreeMarketTimingRepository timingRepository;
    private final ScheduledRajdhaniResultRepository resultRepository;

    public RajshreeMarketTimingsService(
            RajshreeMarketTimingRepository timingRepository,
            ScheduledRajdhaniResultRepository resultRepository
    ) {
        this.timingRepository = timingRepository;
        this.resultRepository = resultRepository;
    }

    @Transactional(readOnly = true)
    public RajshreeMarketTimingsResponse getCurrent() {
        return RajshreeMarketTimingsResponse.from(
                findTiming(RajdhaniMarket.RAJDHAN_DAY),
                findTiming(RajdhaniMarket.RAJDHAN_NIGHT)
        );
    }

    @Transactional(readOnly = true)
    public RajshreeMarketTiming getTiming(RajdhaniMarket market) {
        return findTiming(market);
    }

    @Transactional
    public RajshreeMarketTimingsResponse save(
            RajshreeMarketTimingsRequest request,
            String actorPhone
    ) {
        validateDifferentTimes(request.dayOpenTime(), request.dayCloseTime());
        validateDifferentTimes(request.nightOpenTime(), request.nightCloseTime());

        Instant now = Instant.now();
        RajshreeMarketTiming day = updateTiming(
                RajdhaniMarket.RAJDHAN_DAY,
                request.dayOpenTime(),
                request.dayCloseTime(),
                actorPhone,
                now
        );
        RajshreeMarketTiming night = updateTiming(
                RajdhaniMarket.RAJDHAN_NIGHT,
                request.nightOpenTime(),
                request.nightCloseTime(),
                actorPhone,
                now
        );

        updatePendingResultSchedules(day, now);
        updatePendingResultSchedules(night, now);

        return RajshreeMarketTimingsResponse.from(day, night);
    }

    private RajshreeMarketTiming findTiming(RajdhaniMarket market) {
        return timingRepository.findById(market)
                .orElseGet(() -> defaultTiming(market));
    }

    private RajshreeMarketTiming defaultTiming(RajdhaniMarket market) {
        RajshreeMarketTiming timing = new RajshreeMarketTiming();
        timing.setMarket(market);
        timing.setOpenTime(market.getOpenTime());
        timing.setCloseTime(market.getCloseTime());
        return timing;
    }

    private RajshreeMarketTiming updateTiming(
            RajdhaniMarket market,
            LocalTime openTime,
            LocalTime closeTime,
            String actorPhone,
            Instant now
    ) {
        RajshreeMarketTiming timing = findTiming(market);
        timing.setOpenTime(openTime);
        timing.setCloseTime(closeTime);
        timing.setUpdatedAt(now);
        timing.setUpdatedByPhone(actorPhone);
        return timingRepository.save(timing);
    }

    private void updatePendingResultSchedules(RajshreeMarketTiming timing, Instant now) {
        RajdhaniMarket market = timing.getMarket();
        List<ScheduledRajdhaniResult> openPending =
                resultRepository.findAllByMarketAndOpenPublishedFalse(market);
        for (ScheduledRajdhaniResult result : openPending) {
            result.setOpenPublishAt(atIndiaTime(
                    result.getResultDate(),
                    timing.getOpenTime()
            ));
            result.setUpdatedAt(now);
        }
        resultRepository.saveAll(openPending);

        List<ScheduledRajdhaniResult> closePending =
                resultRepository.findAllByMarketAndPublishedFalse(market);
        for (ScheduledRajdhaniResult result : closePending) {
            result.setPublishAt(atIndiaTime(
                    result.getResultDate(),
                    timing.getCloseTime(),
                    timing.getOpenTime()
            ));
            result.setUpdatedAt(now);
        }
        resultRepository.saveAll(closePending);
    }

    private Instant atIndiaTime(LocalDate date, LocalTime time) {
        return date.atTime(time).atZone(INDIA_ZONE).toInstant();
    }

    private Instant atIndiaTime(LocalDate date, LocalTime time, LocalTime openTime) {
        LocalDate effectiveDate = time.isBefore(openTime) ? date.plusDays(1) : date;
        return atIndiaTime(effectiveDate, time);
    }

    private void validateDifferentTimes(LocalTime openTime, LocalTime closeTime) {
        if (openTime.equals(closeTime)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Each market's open and close times must be different."
            );
        }
    }
}
