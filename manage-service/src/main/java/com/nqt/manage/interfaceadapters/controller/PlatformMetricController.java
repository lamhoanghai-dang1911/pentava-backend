package com.nqt.manage.interfaceadapters.controller;

import com.nqt.manage.usecase.boundary.PlatformMetricInputPort;
import com.nqt.manage.usecase.dto.PlatformMetricRequestDTO;
import com.nqt.manage.usecase.dto.PlatformMetricResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Manage - Platform Metrics", description = "Quản lý các chỉ số thống kê & Social Proof trên Landing Page")
@RestController
@RequestMapping("/api/manage/metrics")
@RequiredArgsConstructor
public class PlatformMetricController {

    private final PlatformMetricInputPort platformMetricInputPort;

    @Operation(summary = "Lấy danh sách các chỉ số đang hoạt động (Public cho Landing Page)")
    @GetMapping
    public ResponseEntity<List<PlatformMetricResponseDTO>> getActiveMetrics() {
        return ResponseEntity.ok(platformMetricInputPort.getActiveMetrics());
    }

    @Operation(summary = "Lấy toàn bộ chỉ số bao gồm cả ẩn (Admin)")
    @GetMapping("/all")
    public ResponseEntity<List<PlatformMetricResponseDTO>> getAllMetrics() {
        return ResponseEntity.ok(platformMetricInputPort.getAllMetrics());
    }

    @Operation(summary = "Xem chi tiết một chỉ số theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<PlatformMetricResponseDTO> getMetricById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(platformMetricInputPort.getMetricById(id));
    }

    @Operation(summary = "Tạo mới chỉ số thống kê (Admin)")
    @PostMapping
    public ResponseEntity<PlatformMetricResponseDTO> createMetric(@Valid @RequestBody PlatformMetricRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(platformMetricInputPort.createMetric(requestDTO));
    }

    @Operation(summary = "Cập nhật chỉ số thống kê (Admin)")
    @PutMapping("/{id}")
    public ResponseEntity<PlatformMetricResponseDTO> updateMetric(@PathVariable("id") Long id,
                                                                  @Valid @RequestBody PlatformMetricRequestDTO requestDTO) {
        return ResponseEntity.ok(platformMetricInputPort.updateMetric(id, requestDTO));
    }

    @Operation(summary = "Xóa một chỉ số thống kê (Admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMetric(@PathVariable("id") Long id) {
        platformMetricInputPort.deleteMetric(id);
        return ResponseEntity.noContent().build();
    }
}
