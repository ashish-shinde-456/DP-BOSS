package com.ashish.resultswebsite.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ResultChangeAuditRepository extends JpaRepository<ResultChangeAudit, Long> {

    List<ResultChangeAudit> findAllByChangedAtGreaterThanEqualOrderByChangedAtDesc(Instant since);
}
