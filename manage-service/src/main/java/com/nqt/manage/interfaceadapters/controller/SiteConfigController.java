package com.nqt.manage.interfaceadapters.controller;

import com.nqt.manage.usecase.boundary.SiteConfigInputPort;
import com.nqt.manage.usecase.dto.SiteConfigRequestDTO;
import com.nqt.manage.usecase.dto.SiteConfigResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Manage - Site Configs", description = "Quản lý cấu hình chung website, thông tin chân trang và mạng xã hội")
@RestController
@RequestMapping("/api/manage/configs")
@RequiredArgsConstructor
public class SiteConfigController {

    private final SiteConfigInputPort siteConfigInputPort;

    @Operation(summary = "Lấy toàn bộ cấu hình công khai dạng Key-Value (Public cho Frontend)")
    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> getPublicConfigs() {
        return ResponseEntity.ok(siteConfigInputPort.getPublicConfigsMap());
    }

    @Operation(summary = "Lấy toàn bộ danh sách cấu hình chi tiết (Admin)")
    @GetMapping
    public ResponseEntity<List<SiteConfigResponseDTO>> getAllConfigs() {
        return ResponseEntity.ok(siteConfigInputPort.getAllConfigs());
    }

    @Operation(summary = "Lấy giá trị cấu hình theo Key")
    @GetMapping("/{key}")
    public ResponseEntity<SiteConfigResponseDTO> getConfigByKey(@PathVariable("key") String key) {
        return ResponseEntity.ok(siteConfigInputPort.getConfigByKey(key));
    }

    @Operation(summary = "Lưu hoặc cập nhật một cấu hình (Admin)")
    @PostMapping
    public ResponseEntity<SiteConfigResponseDTO> saveConfig(@Valid @RequestBody SiteConfigRequestDTO requestDTO) {
        return ResponseEntity.ok(siteConfigInputPort.saveConfig(requestDTO));
    }

    @Operation(summary = "Xóa một cấu hình (Admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable("id") Long id) {
        siteConfigInputPort.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }
}
