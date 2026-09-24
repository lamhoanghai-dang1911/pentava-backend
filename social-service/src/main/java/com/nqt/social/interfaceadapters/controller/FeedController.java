package com.nqt.social.interfaceadapters.controller;

import com.nqt.social.infrastructure.security.CurrentUserProvider;
import com.nqt.social.usecase.boundary.FeedInputPort;
import com.nqt.social.usecase.dto.FeedResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Social - Feed", description = "Bảng tin clip cá nhân hoá (Thuật toán For You & Bảng tin Bạn bè)")
@RestController
@RequestMapping("/api/social/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedInputPort feedInputPort;
    private final CurrentUserProvider currentUserProvider;

    @Operation(
            summary = "Bảng tin đề xuất For You (Infinite Scroll)",
            description = "Thuật toán tham khảo TikTok / Reels: Chấm điểm dựa trên tương tác (High-Five, Comment), độ tươi mới (Time-Decay), hệ số bạn bè và cơ chế Cold-Start. Phân trang cursor-based."
    )
    @GetMapping("/for-you")
    public ResponseEntity<FeedResponseDTO> getForYouFeed(
            @Parameter(description = "Cursor của trang trước (bỏ trống cho trang đầu)")
            @RequestParam(value = "cursor", required = false) String cursor,
            @Parameter(description = "Số lượng clip mỗi trang (mặc định 10, tối đa 50)")
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        Long viewerId = currentUserProvider.getOptionalCurrentUserId().orElse(null);
        return ResponseEntity.ok(feedInputPort.getForYouFeed(viewerId, cursor, limit));
    }

    @Operation(
            summary = "Bảng tin Bạn bè (Friends Feed)",
            description = "Hiển thị clip của bạn bè theo thứ tự thời gian mới nhất (chronological). Phân trang cursor-based."
    )
    @GetMapping("/friends")
    public ResponseEntity<FeedResponseDTO> getFriendsFeed(
            @Parameter(description = "Cursor của trang trước (bỏ trống cho trang đầu)")
            @RequestParam(value = "cursor", required = false) String cursor,
            @Parameter(description = "Số lượng clip mỗi trang (mặc định 10, tối đa 50)")
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        Long viewerId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(feedInputPort.getFriendsFeed(viewerId, cursor, limit));
    }
}
