package com.ashish.resultswebsite.goldenank;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface GoldenAnkResultRepository extends JpaRepository<GoldenAnkResult, Long> {

    Optional<GoldenAnkResult> findByResultDate(LocalDate resultDate);

    Optional<GoldenAnkResult> findFirstByResultDateLessThanEqualOrderByResultDateDesc(
            LocalDate resultDate
    );
}
