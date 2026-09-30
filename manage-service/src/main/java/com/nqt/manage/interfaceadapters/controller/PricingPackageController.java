package com.nqt.manage.interfaceadapters.controller;

import com.nqt.manage.usecase.boundary.PricingPackageInputPort;
import com.nqt.manage.usecase.dto.PricingPackageRequestDTO;
import com.nqt.manage.usecase.dto.PricingPackageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Manage - Pricing Packages", description = "Quản lý hiển thị các gói nạp Ruby / Bảng giá trên Landing Page")
@RestController
@RequestMapping("/api/manage/pricing")
@RequiredArgsConstructor
public class PricingPackageController {

    private final PricingPackageInputPort pricingPackageInputPort;

    @Operation(summary = "Lấy danh sách Gói nạp đang hoạt động (Public cho Landing Page)")
    @GetMapping
    public ResponseEntity<List<PricingPackageResponseDTO>> getActivePricingPackages() {
        return ResponseEntity.ok(pricingPackageInputPort.getActivePricingPackages());
    }

    @Operation(summary = "Lấy toàn bộ Gói nạp bao gồm cả ẩn (Dành cho Admin Web)")
    @GetMapping("/all")
    public ResponseEntity<List<PricingPackageResponseDTO>> getAllPricingPackages() {
        return ResponseEntity.ok(pricingPackageInputPort.getAllPricingPackages());
    }

    @Operation(summary = "Xem chi tiết một Gói nạp")
    @GetMapping("/{id}")
    public ResponseEntity<PricingPackageResponseDTO> getPricingPackageById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(pricingPackageInputPort.getPricingPackageById(id));
    }

    @Operation(summary = "Tạo mới Gói nạp (Admin)")
    @PostMapping
    public ResponseEntity<PricingPackageResponseDTO> createPricingPackage(@Valid @RequestBody PricingPackageRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pricingPackageInputPort.createPricingPackage(requestDTO));
    }

    @Operation(summary = "Cập nhật Gói nạp (Admin)")
    @PutMapping("/{id}")
    public ResponseEntity<PricingPackageResponseDTO> updatePricingPackage(@PathVariable("id") Long id,
                                                                          @Valid @RequestBody PricingPackageRequestDTO requestDTO) {
        return ResponseEntity.ok(pricingPackageInputPort.updatePricingPackage(id, requestDTO));
    }

    @Operation(summary = "Xóa Gói nạp (Admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePricingPackage(@PathVariable("id") Long id) {
        pricingPackageInputPort.deletePricingPackage(id);
        return ResponseEntity.noContent().build();
    }
}
