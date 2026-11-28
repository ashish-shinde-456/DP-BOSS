package com.ashish.resultswebsite.rajdhani;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduledRajdhaniResultRepository
        extends JpaRepository<ScheduledRajdhaniResult, Long> {

    Optional<ScheduledRajdhaniResult> findByMarketAndResultDate(
            RajdhaniMarket market,
            LocalDate resultDate
    );

    Optional<ScheduledRajdhaniResult> findFirstByMarketAndOpenPublishedTrueOrderByResultDateDesc(
            RajdhaniMarket market
    );

    List<ScheduledRajdhaniResult> findAllByResultDateOrderByMarketAsc(LocalDate resultDate);

    List<ScheduledRajdhaniResult> findAllByMarketAndOpenPublishedFalse(RajdhaniMarket market);

    List<ScheduledRajdhaniResult> findAllByMarketAndPublishedFalse(RajdhaniMarket market);

    List<ScheduledRajdhaniResult> findAllByOpenPublishedFalseAndOpenPublishAtLessThanEqual(
            Instant now
    );

    List<ScheduledRajdhaniResult>
    findAllByPublishedFalseAndPublishAtLessThanEqualAndClosePanaIsNotNullAndCloseDigitIsNotNull(
            Instant now
    );
}
