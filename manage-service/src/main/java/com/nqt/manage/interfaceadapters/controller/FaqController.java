package com.nqt.manage.interfaceadapters.controller;

import com.nqt.manage.usecase.boundary.FaqInputPort;
import com.nqt.manage.usecase.dto.FaqRequestDTO;
import com.nqt.manage.usecase.dto.FaqResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Manage - FAQs", description = "Quản lý câu hỏi thường gặp trên Landing Page")
@RestController
@RequestMapping("/api/manage/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqInputPort faqInputPort;

    @Operation(summary = "Lấy danh sách Câu hỏi FAQ đang hoạt động (Public)")
    @GetMapping
    public ResponseEntity<List<FaqResponseDTO>> getActiveFaqs(@RequestParam(value = "category", required = false) String category) {
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(faqInputPort.getFaqsByCategory(category));
        }
        return ResponseEntity.ok(faqInputPort.getActiveFaqs());
    }

    @Operation(summary = "Lấy toàn bộ Câu hỏi FAQ bao gồm cả ẩn (Admin)")
    @GetMapping("/all")
    public ResponseEntity<List<FaqResponseDTO>> getAllFaqs() {
        return ResponseEntity.ok(faqInputPort.getAllFaqs());
    }

    @Operation(summary = "Xem chi tiết một Câu hỏi FAQ")
    @GetMapping("/{id}")
    public ResponseEntity<FaqResponseDTO> getFaqById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(faqInputPort.getFaqById(id));
    }

    @Operation(summary = "Tạo mới Câu hỏi FAQ (Admin)")
    @PostMapping
    public ResponseEntity<FaqResponseDTO> createFaq(@Valid @RequestBody FaqRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(faqInputPort.createFaq(requestDTO));
    }

    @Operation(summary = "Cập nhật Câu hỏi FAQ (Admin)")
    @PutMapping("/{id}")
    public ResponseEntity<FaqResponseDTO> updateFaq(@PathVariable("id") Long id,
                                                    @Valid @RequestBody FaqRequestDTO requestDTO) {
        return ResponseEntity.ok(faqInputPort.updateFaq(id, requestDTO));
    }

    @Operation(summary = "Xóa Câu hỏi FAQ (Admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable("id") Long id) {
        faqInputPort.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }
}
