package com.ashish.resultswebsite.rajdhani;

import com.ashish.resultswebsite.admin.AdminUserRepository;
import com.ashish.resultswebsite.admin.AdminUserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RajdhaniResultController {

    private final RajdhaniResultService service;
    private final AdminUserRepository adminUserRepository;
    private final RajshreeMarketTimingsService timingsService;

    public RajdhaniResultController(
            RajdhaniResultService service,
            AdminUserRepository adminUserRepository,
            RajshreeMarketTimingsService timingsService
    ) {
        this.service = service;
        this.adminUserRepository = adminUserRepository;
        this.timingsService = timingsService;
    }

    @GetMapping({"/markets/rajshree-day", "/markets/rajdhan-day"})
    public ResponseEntity<RajdhaniResultResponse> getRajdhaniDay() {
        return publicResult(RajdhaniMarket.RAJDHAN_DAY);
    }

    @GetMapping({"/markets/rajshree-night", "/markets/rajdhan-night"})
    public ResponseEntity<RajdhaniResultResponse> getRajdhaniNight() {
        return publicResult(RajdhaniMarket.RAJDHAN_NIGHT);
    }

    @GetMapping("/market-timings")
    public RajshreeMarketTimingsResponse getMarketTimings() {
        return timingsService.getCurrent();
    }

    @PostMapping("/admin/market-timings")
    public RajshreeMarketTimingsResponse saveMarketTimings(
            @Valid @RequestBody RajshreeMarketTimingsRequest request,
            Authentication authentication
    ) {
        return timingsService.save(request, authentication.getName());
    }

    @GetMapping("/admin/me")
    public AdminUserResponse getCurrentAdmin(Authentication authentication) {
        return adminUserRepository.findById(authentication.getName())
                .map(AdminUserResponse::from)
                .orElseThrow(() -> new IllegalStateException("Authenticated admin account was not found."));
    }

    @GetMapping({"/admin/rajshree-results", "/admin/rajdhani-results"})
    public List<RajdhaniResultResponse> getAdminResults(
            @RequestParam LocalDate date
    ) {
        return service.getResultsForDate(date);
    }

    @PostMapping({
            "/admin/markets/rajshree-day/results",
            "/admin/markets/rajdhan-day/results"
    })
    public RajdhaniResultResponse saveRajdhaniDay(
            @Valid @RequestBody RajdhaniResultRequest request,
            Authentication authentication
    ) {
        return service.save(RajdhaniMarket.RAJDHAN_DAY, request, authentication.getName());
    }

    @PostMapping({
            "/admin/markets/rajshree-night/results",
            "/admin/markets/rajdhan-night/results"
    })
    public RajdhaniResultResponse saveRajdhaniNight(
            @Valid @RequestBody RajdhaniResultRequest request,
            Authentication authentication
    ) {
        return service.save(RajdhaniMarket.RAJDHAN_NIGHT, request, authentication.getName());
    }

    private ResponseEntity<RajdhaniResultResponse> publicResult(RajdhaniMarket market) {
        return service.getLatestOpenPublished(market)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
