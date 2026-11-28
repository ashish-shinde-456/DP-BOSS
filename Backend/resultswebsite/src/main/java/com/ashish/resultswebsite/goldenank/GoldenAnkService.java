package com.ashish.resultswebsite.goldenank;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class GoldenAnkService {

    private static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");

    private final GoldenAnkResultRepository repository;

    public GoldenAnkService(GoldenAnkResultRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<GoldenAnkResponse> getCurrent() {
        LocalDate today = LocalDate.now(INDIA_ZONE);
        return repository
                .findFirstByResultDateLessThanEqualOrderByResultDateDesc(today)
                .map(GoldenAnkResponse::from);
    }

    @Transactional(readOnly = true)
    public Optional<GoldenAnkResponse> getForDate(LocalDate resultDate) {
        return repository.findByResultDate(resultDate).map(GoldenAnkResponse::from);
    }

    @Transactional
    public GoldenAnkResponse save(GoldenAnkRequest request) {
        LocalDate today = LocalDate.now(INDIA_ZONE);
        if (request.resultDate().isBefore(today)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Golden Ank can only be scheduled for today or a future date."
            );
        }

        GoldenAnkResult result = repository.findByResultDate(request.resultDate())
                .orElseGet(GoldenAnkResult::new);
        result.setResultDate(request.resultDate());
        result.setAnkOne(request.ankOne());
        result.setAnkTwo(request.ankTwo());
        result.setAnkThree(request.ankThree());
        result.setAnkFour(request.ankFour());
        result.setUpdatedAt(Instant.now());

        return GoldenAnkResponse.from(repository.save(result));
    }
}
