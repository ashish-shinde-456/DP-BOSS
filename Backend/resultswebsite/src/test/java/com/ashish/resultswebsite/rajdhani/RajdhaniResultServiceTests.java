package com.ashish.resultswebsite.rajdhani;

import com.ashish.resultswebsite.admin.AdminUser;
import com.ashish.resultswebsite.admin.AdminUserRepository;
import com.ashish.resultswebsite.audit.ResultChangeAuditRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RajdhaniResultServiceTests {

    @Test
    void savesOpenAndCloseStagesWithoutReplacingOpenValues() {
        ScheduledRajdhaniResultRepository repository =
                mock(ScheduledRajdhaniResultRepository.class);
        AdminUserRepository adminUserRepository = mock(AdminUserRepository.class);
        ResultChangeAuditRepository auditRepository = mock(ResultChangeAuditRepository.class);
        AtomicReference<ScheduledRajdhaniResult> storedResult = new AtomicReference<>();
        LocalDate resultDate = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(1);
        AdminUser admin = new AdminUser();
        admin.setPhoneNumber("1234567890");
        admin.setDisplayName("Admin");
        admin.setActive(true);

        when(adminUserRepository.findById("1234567890")).thenReturn(Optional.of(admin));
        when(repository.findByMarketAndResultDate(RajdhaniMarket.RAJDHAN_DAY, resultDate))
                .thenAnswer(invocation -> Optional.ofNullable(storedResult.get()));
        when(repository.save(any(ScheduledRajdhaniResult.class))).thenAnswer(invocation -> {
            ScheduledRajdhaniResult result = invocation.getArgument(0);
            storedResult.set(result);
            return result;
        });

        RajdhaniResultService service = new RajdhaniResultService(
                repository,
                adminUserRepository,
                auditRepository,
                new RajshreeMarketTimingsService(
                        mock(RajshreeMarketTimingRepository.class),
                        repository
                )
        );
        RajdhaniResultResponse openResponse = service.save(
                RajdhaniMarket.RAJDHAN_DAY,
                new RajdhaniResultRequest(resultDate, "260", "8", null, null),
                "1234567890"
        );
        RajdhaniResultResponse completeResponse = service.save(
                RajdhaniMarket.RAJDHAN_DAY,
                new RajdhaniResultRequest(resultDate, null, null, "468", "4"),
                "1234567890"
        );

        assertEquals("260-8", openResponse.formattedResult());
        assertEquals("260-84-468", completeResponse.formattedResult());
        assertEquals("260", storedResult.get().getOpenPana());
        assertEquals("8", storedResult.get().getOpenDigit());
        assertEquals(
                resultDate.atTime(15, 15).atZone(ZoneId.of("Asia/Kolkata")).toInstant(),
                storedResult.get().getOpenPublishAt()
        );
        assertEquals(
                resultDate.atTime(17, 15).atZone(ZoneId.of("Asia/Kolkata")).toInstant(),
                storedResult.get().getPublishAt()
        );
        assertFalse(completeResponse.openPublished());
        assertFalse(completeResponse.published());
    }

    @Test
    void scheduledPublisherPublishesOpenAndClosingStagesSeparately() {
        ScheduledRajdhaniResultRepository repository =
                mock(ScheduledRajdhaniResultRepository.class);
        ScheduledRajdhaniResult result = new ScheduledRajdhaniResult();
        result.setMarket(RajdhaniMarket.RAJDHAN_NIGHT);
        result.setResultDate(LocalDate.now(ZoneId.of("Asia/Kolkata")));
        result.setOpenPana("260");
        result.setOpenDigit("8");
        result.setClosePana("468");
        result.setCloseDigit("4");
        result.setOpenPublishAt(Instant.now().minusSeconds(1));
        result.setPublishAt(Instant.now().minusSeconds(1));

        when(repository.findAllByOpenPublishedFalseAndOpenPublishAtLessThanEqual(any(Instant.class)))
                .thenReturn(List.of(result));
        when(repository
                .findAllByPublishedFalseAndPublishAtLessThanEqualAndClosePanaIsNotNullAndCloseDigitIsNotNull(
                        any(Instant.class)
                )).thenReturn(List.of(result));

        RajdhaniResultService service = new RajdhaniResultService(
                repository,
                mock(AdminUserRepository.class),
                mock(ResultChangeAuditRepository.class),
                new RajshreeMarketTimingsService(
                        mock(RajshreeMarketTimingRepository.class),
                        repository
                )
        );
        service.publishDueResults();

        assertTrue(result.isOpenPublished());
        assertTrue(result.isPublished());
        assertNotNull(result.getOpenPublishedAt());
        assertNotNull(result.getPublishedAt());
    }
}
