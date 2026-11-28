package com.ashish.resultswebsite.audit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
public class ResultChangeAuditController {

    private final ResultChangeAuditService service;

    public ResultChangeAuditController(ResultChangeAuditService service) {
        this.service = service;
    }

    @GetMapping
    public List<ResultChangeAuditResponse> getRecentChanges() {
        return service.getRecentChanges();
    }
}
