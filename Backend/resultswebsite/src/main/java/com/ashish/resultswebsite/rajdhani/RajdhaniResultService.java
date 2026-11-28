package com.ashish.resultswebsite.rajdhani;

import com.ashish.resultswebsite.admin.AdminUser;
import com.ashish.resultswebsite.admin.AdminUserRepository;
import com.ashish.resultswebsite.audit.ResultChangeAudit;
import com.ashish.resultswebsite.audit.ResultChangeAuditRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class RajdhaniResultService {

    private static final ZoneId PUBLISH_ZONE = ZoneId.of("Asia/Kolkata");

    private final ScheduledRajdhaniResultRepository repository;
    private final AdminUserRepository adminUserRepository;
    private final ResultChangeAuditRepository auditRepository;
    private final RajshreeMarketTimingsService timingsService;

    public RajdhaniResultService(
            ScheduledRajdhaniResultRepository repository,
            AdminUserRepository adminUserRepository,
            ResultChangeAuditRepository auditRepository,
            RajshreeMarketTimingsService timingsService
    ) {
        this.repository = repository;
        this.adminUserRepository = adminUserRepository;
        this.auditRepository = auditRepository;
        this.timingsService = timingsService;
    }

    @Transactional(readOnly = true)
    public Optional<RajdhaniResultResponse> getLatestOpenPublished(RajdhaniMarket market) {
        return repository
                .findFirstByMarketAndOpenPublishedTrueOrderByResultDateDesc(market)
                .map(RajdhaniResultResponse::from);
    }

    @Transactional(readOnly = true)
    public List<RajdhaniResultResponse> getResultsForDate(LocalDate resultDate) {
        return repository.findAllByResultDateOrderByMarketAsc(resultDate)
                .stream()
                .map(RajdhaniResultResponse::from)
                .toList();
    }

    @Transactional
    public RajdhaniResultResponse save(
            RajdhaniMarket market,
            RajdhaniResultRequest request,
            String actorPhone
    ) {
        Instant now = Instant.now();
        RajshreeMarketTiming timing = timingsService.getTiming(market);
        Instant openPublishAt = request.resultDate()
                .atTime(timing.getOpenTime())
                .atZone(PUBLISH_ZONE)
                .toInstant();
        LocalDate closeDate = timing.getCloseTime().isBefore(timing.getOpenTime())
                ? request.resultDate().plusDays(1)
                : request.resultDate();
        Instant closePublishAt = closeDate
                .atTime(timing.getCloseTime())
                .atZone(PUBLISH_ZONE)
                .toInstant();

        AdminUser actor = adminUserRepository.findById(actorPhone)
                .filter(AdminUser::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "This admin account is no longer active."
                ));
        ScheduledRajdhaniResult result = repository
                .findByMarketAndResultDate(market, request.resultDate())
                .orElse(null);
        if (result == null && !request.hasOpenResult()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Save the open pana and digit before the closing result."
            );
        }
        if (result == null) {
            result = new ScheduledRajdhaniResult();
        }
        String previousResult = result.getId() == null
                ? null
                : RajdhaniResultResponse.from(result).formattedResult();
        String action = previousResult == null ? "CREATE" : "UPDATE";

        result.setMarket(market);
        result.setResultDate(request.resultDate());
        result.setOpenPublishAt(openPublishAt);
        result.setPublishAt(closePublishAt);
        if (request.hasOpenResult()) {
            result.setOpenPana(request.openPana());
            result.setOpenDigit(request.openDigit());
        }
        if (request.hasCloseResult()) {
            result.setClosePana(request.closePana());
            result.setCloseDigit(request.closeDigit());
        }

        if (!result.isOpenPublished() && !openPublishAt.isAfter(now)) {
            result.setOpenPublished(true);
            result.setOpenPublishedAt(now);
        }
        boolean hasCompleteCloseResult =
                result.getClosePana() != null && result.getCloseDigit() != null;
        if (!result.isPublished() && hasCompleteCloseResult && !closePublishAt.isAfter(now)) {
            result.setPublished(true);
            result.setPublishedAt(now);
        }
        result.setUpdatedAt(now);

        ScheduledRajdhaniResult savedResult = repository.save(result);
        RajdhaniResultResponse response = RajdhaniResultResponse.from(savedResult);

        ResultChangeAudit audit = new ResultChangeAudit();
        audit.setActorPhone(actor.getPhoneNumber());
        audit.setActorName(actor.getDisplayName());
        audit.setMarketName(market.getDisplayName());
        audit.setResultDate(request.resultDate());
        audit.setAction(action);
        audit.setPreviousResult(previousResult);
        audit.setNewResult(response.formattedResult());
        audit.setChangedAt(now);
        auditRepository.save(audit);

        return response;
    }

    @Transactional
    public void publishDueResults() {
        Instant now = Instant.now();
        List<ScheduledRajdhaniResult> dueOpenResults =
                repository.findAllByOpenPublishedFalseAndOpenPublishAtLessThanEqual(now);
        for (ScheduledRajdhaniResult result : dueOpenResults) {
            result.setOpenPublished(true);
            result.setOpenPublishedAt(now);
            result.setUpdatedAt(now);
        }
        repository.saveAll(dueOpenResults);

        List<ScheduledRajdhaniResult> dueCloseResults =
                repository
                        .findAllByPublishedFalseAndPublishAtLessThanEqualAndClosePanaIsNotNullAndCloseDigitIsNotNull(now);
        for (ScheduledRajdhaniResult result : dueCloseResults) {
            result.setPublished(true);
            result.setPublishedAt(now);
            result.setUpdatedAt(now);
        }
        repository.saveAll(dueCloseResults);
    }
}
