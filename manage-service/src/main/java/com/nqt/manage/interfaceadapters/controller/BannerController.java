package com.nqt.manage.interfaceadapters.controller;

import com.nqt.manage.usecase.boundary.BannerInputPort;
import com.nqt.manage.usecase.dto.BannerRequestDTO;
import com.nqt.manage.usecase.dto.BannerResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Manage - Banners", description = "Quản lý Banner quảng bá trên Landing Page")
@RestController
@RequestMapping("/api/manage/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerInputPort bannerInputPort;

    @Operation(summary = "Lấy danh sách Banner đang hoạt động (Public cho Landing Page)")
    @GetMapping
    public ResponseEntity<List<BannerResponseDTO>> getActiveBanners() {
        return ResponseEntity.ok(bannerInputPort.getActiveBanners());
    }

    @Operation(summary = "Lấy toàn bộ Banner bao gồm cả ẩn (Dành cho Admin Web)")
    @GetMapping("/all")
    public ResponseEntity<List<BannerResponseDTO>> getAllBanners() {
        return ResponseEntity.ok(bannerInputPort.getAllBanners());
    }

    @Operation(summary = "Xem chi tiết một Banner")
    @GetMapping("/{id}")
    public ResponseEntity<BannerResponseDTO> getBannerById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(bannerInputPort.getBannerById(id));
    }

    @Operation(summary = "Tạo mới Banner (Admin)")
    @PostMapping
    public ResponseEntity<BannerResponseDTO> createBanner(@Valid @RequestBody BannerRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bannerInputPort.createBanner(requestDTO));
    }

    @Operation(summary = "Cập nhật Banner (Admin)")
    @PutMapping("/{id}")
    public ResponseEntity<BannerResponseDTO> updateBanner(@PathVariable("id") Long id,
                                                          @Valid @RequestBody BannerRequestDTO requestDTO) {
        return ResponseEntity.ok(bannerInputPort.updateBanner(id, requestDTO));
    }

    @Operation(summary = "Xóa Banner (Admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable("id") Long id) {
        bannerInputPort.deleteBanner(id);
        return ResponseEntity.noContent().build();
    }
}
