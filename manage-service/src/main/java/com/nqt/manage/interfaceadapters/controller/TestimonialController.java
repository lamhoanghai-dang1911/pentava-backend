package com.nqt.manage.interfaceadapters.controller;

import com.nqt.manage.usecase.boundary.TestimonialInputPort;
import com.nqt.manage.usecase.dto.TestimonialRequestDTO;
import com.nqt.manage.usecase.dto.TestimonialResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Manage - Testimonials", description = "Quản lý cảm nhận, đánh giá của người dùng trên Landing Page")
@RestController
@RequestMapping("/api/manage/testimonials")
@RequiredArgsConstructor
public class TestimonialController {

    private final TestimonialInputPort testimonialInputPort;

    @Operation(summary = "Lấy danh sách Đánh giá đang hiển thị (Public cho Landing Page)")
    @GetMapping
    public ResponseEntity<List<TestimonialResponseDTO>> getActiveTestimonials() {
        return ResponseEntity.ok(testimonialInputPort.getActiveTestimonials());
    }

    @Operation(summary = "Lấy toàn bộ Đánh giá bao gồm cả ẩn (Dành cho Admin Web)")
    @GetMapping("/all")
    public ResponseEntity<List<TestimonialResponseDTO>> getAllTestimonials() {
        return ResponseEntity.ok(testimonialInputPort.getAllTestimonials());
    }

    @Operation(summary = "Xem chi tiết một Đánh giá")
    @GetMapping("/{id}")
    public ResponseEntity<TestimonialResponseDTO> getTestimonialById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(testimonialInputPort.getTestimonialById(id));
    }

    @Operation(summary = "Tạo mới Đánh giá (Admin - tự động đồng bộ name từ onboarding-service nếu có userId)")
    @PostMapping
    public ResponseEntity<TestimonialResponseDTO> createTestimonial(@Valid @RequestBody TestimonialRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(testimonialInputPort.createTestimonial(requestDTO));
    }

    @Operation(summary = "Cập nhật Đánh giá (Admin)")
    @PutMapping("/{id}")
    public ResponseEntity<TestimonialResponseDTO> updateTestimonial(@PathVariable("id") Long id,
                                                                    @Valid @RequestBody TestimonialRequestDTO requestDTO) {
        return ResponseEntity.ok(testimonialInputPort.updateTestimonial(id, requestDTO));
    }

    @Operation(summary = "Xóa Đánh giá (Admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestimonial(@PathVariable("id") Long id) {
        testimonialInputPort.deleteTestimonial(id);
        return ResponseEntity.noContent().build();
    }
}
