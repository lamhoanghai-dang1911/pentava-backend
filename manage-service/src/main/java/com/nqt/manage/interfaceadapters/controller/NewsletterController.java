package com.nqt.manage.interfaceadapters.controller;

import com.nqt.manage.usecase.boundary.NewsletterInputPort;
import com.nqt.manage.usecase.dto.NewsletterResponseDTO;
import com.nqt.manage.usecase.dto.SubscribeNewsletterRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Manage - Newsletters", description = "Quản lý danh sách đăng ký nhận tin từ Landing Page")
@RestController
@RequestMapping("/api/manage/newsletters")
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterInputPort newsletterInputPort;

    @Operation(summary = "Khách hàng đăng ký nhận bản tin từ Landing Page (Public)")
    @PostMapping
    public ResponseEntity<NewsletterResponseDTO> subscribe(@Valid @RequestBody SubscribeNewsletterRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(newsletterInputPort.subscribe(requestDTO));
    }

    @Operation(summary = "Lấy toàn bộ danh sách email đăng ký nhận tin (Admin)")
    @GetMapping
    public ResponseEntity<List<NewsletterResponseDTO>> getAllSubscriptions() {
        return ResponseEntity.ok(newsletterInputPort.getAllSubscriptions());
    }

    @Operation(summary = "Hủy đăng ký nhận tin cho một email (Admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unsubscribe(@PathVariable("id") Long id) {
        newsletterInputPort.unsubscribe(id);
        return ResponseEntity.noContent().build();
    }
}
