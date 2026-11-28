package com.ashish.resultswebsite.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ResultChangeAuditService {

    private final ResultChangeAuditRepository repository;

    public ResultChangeAuditService(ResultChangeAuditRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ResultChangeAuditResponse> getRecentChanges() {
        Instant fiveDaysAgo = Instant.now().minus(5, ChronoUnit.DAYS);
        return repository.findAllByChangedAtGreaterThanEqualOrderByChangedAtDesc(fiveDaysAgo)
                .stream()
                .map(ResultChangeAuditResponse::from)
                .toList();
    }
}
