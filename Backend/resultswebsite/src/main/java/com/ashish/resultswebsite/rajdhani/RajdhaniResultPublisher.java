package com.ashish.resultswebsite.rajdhani;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RajdhaniResultPublisher {

    private final RajdhaniResultService service;

    public RajdhaniResultPublisher(RajdhaniResultService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${app.results.publish-check-ms:1000}")
    public void publishDueResults() {
        service.publishDueResults();
    }
}
