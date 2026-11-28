package com.ashish.resultswebsite.market;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MarketResultRepository extends JpaRepository<MarketResult, Long> {

    Optional<MarketResult> findByMarketNameIgnoreCaseAndResultDate(
            String marketName,
            LocalDate resultDate
    );

    List<MarketResult> findAllByOrderByResultDateDescUpdatedAtDesc();
}
