package com.nqt.manage.interfaceadapters.controller;

import com.nqt.manage.usecase.boundary.LandingOverviewInputPort;
import com.nqt.manage.usecase.dto.LandingOverviewResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Landing Page - Overview", description = "API tổng hợp toàn bộ dữ liệu hiển thị trên trang chủ Landing Page")
@RestController
@RequestMapping("/api/manage/landing")
@RequiredArgsConstructor
public class LandingOverviewController {

    private final LandingOverviewInputPort landingOverviewInputPort;

    @Operation(summary = "Lấy dữ liệu tổng hợp cho trang chủ Landing Page (Banners, Features, Testimonials, FAQs, Pricing, Metrics, Configs)")
    @GetMapping("/overview")
    public ResponseEntity<LandingOverviewResponseDTO> getLandingOverview() {
        return ResponseEntity.ok(landingOverviewInputPort.getLandingOverview());
    }
}
