package com.ashish.resultswebsite.market;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class MarketResultService {

    private final MarketResultRepository repository;

    public MarketResultService(MarketResultRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<MarketResultResponse> getLatestPerMarket() {
        Map<String, MarketResultResponse> latestResults = new LinkedHashMap<>();

        for (MarketResult result : repository.findAllByOrderByResultDateDescUpdatedAtDesc()) {
            if (result.getOpenPana() == null || result.getOpenDigit() == null) continue;

            String marketKey = normalizeMarketName(result.getMarketName());
            latestResults.putIfAbsent(marketKey, MarketResultResponse.from(result));
        }

        return new ArrayList<>(latestResults.values());
    }

    @Transactional
    public MarketResultResponse upsert(MarketResultWebhook webhook) {
        String marketName = webhook.marketName().trim().replaceAll("\\s+", " ");
        MarketResult result = repository
                .findByMarketNameIgnoreCaseAndResultDate(marketName, webhook.resultDate())
                .orElseGet(MarketResult::new);

        result.setMarketName(marketName);
        result.setResultDate(webhook.resultDate());
        if (webhook.apiOpenPana() != null) {
            result.setOpenPana(webhook.apiOpenPana());
            result.setOpenDigit(webhook.apiOpenDigit());
        }
        if (webhook.apiClosePana() != null) {
            result.setClosePana(webhook.apiClosePana());
            result.setCloseDigit(webhook.apiCloseDigit());
        }
        result.setUpdatedAt(Instant.now());

        return MarketResultResponse.from(repository.save(result));
    }

    private String normalizeMarketName(String marketName) {
        return marketName.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }
}
