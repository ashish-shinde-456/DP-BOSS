package com.ashish.resultswebsite.audit;

import java.time.Instant;
import java.time.LocalDate;

public record ResultChangeAuditResponse(
        Long id,
        String actorPhone,
        String actorName,
        String marketName,
        LocalDate resultDate,
        String action,
        String previousResult,
        String newResult,
        Instant changedAt
) {

    public static ResultChangeAuditResponse from(ResultChangeAudit audit) {
        return new ResultChangeAuditResponse(
                audit.getId(),
                audit.getActorPhone(),
                audit.getActorName(),
                audit.getMarketName(),
                audit.getResultDate(),
                audit.getAction(),
                audit.getPreviousResult(),
                audit.getNewResult(),
                audit.getChangedAt()
        );
    }
}
