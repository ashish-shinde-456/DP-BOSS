package com.ashish.resultswebsite.goldenank;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class GoldenAnkController {

    private final GoldenAnkService service;

    public GoldenAnkController(GoldenAnkService service) {
        this.service = service;
    }

    @GetMapping("/golden-ank")
    public ResponseEntity<GoldenAnkResponse> getCurrent() {
        return service.getCurrent()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/admin/golden-ank")
    public ResponseEntity<GoldenAnkResponse> getForDate(@RequestParam LocalDate date) {
        return service.getForDate(date)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/admin/golden-ank")
    public GoldenAnkResponse save(@Valid @RequestBody GoldenAnkRequest request) {
        return service.save(request);
    }
}
