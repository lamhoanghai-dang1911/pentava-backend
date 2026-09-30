package com.nqt.manage.interfaceadapters.controller;

import com.nqt.manage.usecase.boundary.FeatureInputPort;
import com.nqt.manage.usecase.dto.FeatureRequestDTO;
import com.nqt.manage.usecase.dto.FeatureResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Manage - Features", description = "Quản lý các tính năng nổi bật giới thiệu trên Landing Page")
@RestController
@RequestMapping("/api/manage/features")
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureInputPort featureInputPort;

    @Operation(summary = "Lấy danh sách Tính năng đang hoạt động (Public cho Landing Page)")
    @GetMapping
    public ResponseEntity<List<FeatureResponseDTO>> getActiveFeatures() {
        return ResponseEntity.ok(featureInputPort.getActiveFeatures());
    }

    @Operation(summary = "Lấy toàn bộ Tính năng bao gồm cả ẩn (Dành cho Admin Web)")
    @GetMapping("/all")
    public ResponseEntity<List<FeatureResponseDTO>> getAllFeatures() {
        return ResponseEntity.ok(featureInputPort.getAllFeatures());
    }

    @Operation(summary = "Xem chi tiết một Tính năng")
    @GetMapping("/{id}")
    public ResponseEntity<FeatureResponseDTO> getFeatureById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(featureInputPort.getFeatureById(id));
    }

    @Operation(summary = "Tạo mới Tính năng (Admin)")
    @PostMapping
    public ResponseEntity<FeatureResponseDTO> createFeature(@Valid @RequestBody FeatureRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(featureInputPort.createFeature(requestDTO));
    }

    @Operation(summary = "Cập nhật Tính năng (Admin)")
    @PutMapping("/{id}")
    public ResponseEntity<FeatureResponseDTO> updateFeature(@PathVariable("id") Long id,
                                                            @Valid @RequestBody FeatureRequestDTO requestDTO) {
        return ResponseEntity.ok(featureInputPort.updateFeature(id, requestDTO));
    }

    @Operation(summary = "Xóa Tính năng (Admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeature(@PathVariable("id") Long id) {
        featureInputPort.deleteFeature(id);
        return ResponseEntity.noContent().build();
    }
}
