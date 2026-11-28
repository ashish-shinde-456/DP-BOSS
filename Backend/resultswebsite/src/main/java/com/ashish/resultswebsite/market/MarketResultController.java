package com.ashish.resultswebsite.market;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MarketResultController {

    private static final String WEBHOOK_SECRET_HEADER = "X-Webhook-Secret";

    private final MarketResultService service;
    private final String webhookSecret;

    public MarketResultController(
            MarketResultService service,
            @Value("${app.webhook.secret:}") String webhookSecret
    ) {
        this.service = service;
        this.webhookSecret = webhookSecret;
    }

    @GetMapping("/market-results")
    public List<MarketResultResponse> getMarketResults() {
        return service.getLatestPerMarket();
    }

    @PostMapping("/webhooks/market-result")
    public ResponseEntity<MarketResultResponse> receiveMarketResult(
            @RequestHeader(name = WEBHOOK_SECRET_HEADER, required = false) String suppliedSecret,
            @Valid @RequestBody MarketResultWebhook webhook
    ) {
        verifyWebhookSecret(suppliedSecret);
        return ResponseEntity.ok(service.upsert(webhook));
    }

    private void verifyWebhookSecret(String suppliedSecret) {
        if (webhookSecret.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Webhook authentication is not configured."
            );
        }

        if (suppliedSecret == null || !MessageDigest.isEqual(
                webhookSecret.getBytes(StandardCharsets.UTF_8),
                suppliedSecret.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid webhook secret.");
        }
    }
}
