package com.nqt.social.interfaceadapters.controller;

import com.nqt.social.infrastructure.realtime.SseNotificationService;
import com.nqt.social.infrastructure.security.CurrentUserProvider;
import com.nqt.social.usecase.boundary.NotificationInputPort;
import com.nqt.social.usecase.dto.NotificationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@Tag(name = "Social - Notifications", description = "Quản lý thông báo và Server-Sent Events (SSE)")
@RestController
@RequestMapping("/api/social/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationInputPort notificationInputPort;
    private final SseNotificationService sseNotificationService;
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "Lấy danh sách thông báo của người dùng (phân trang)")
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(notificationInputPort.getNotifications(userId, page, size));
    }

    @Operation(summary = "Lấy số lượng thông báo chưa đọc")
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        Long userId = currentUserProvider.resolveCurrentUserId();
        long unreadCount = notificationInputPort.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }

    @Operation(summary = "Đánh dấu một thông báo đã đọc")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable("id") Long id) {
        Long userId = currentUserProvider.resolveCurrentUserId();
        notificationInputPort.markAsRead(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Đánh dấu tất cả thông báo đã đọc")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        Long userId = currentUserProvider.resolveCurrentUserId();
        notificationInputPort.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Đăng ký nhận thông báo realtime qua SSE",
            description = "Frontend mở kết nối EventSource tới endpoint này để nhận thông báo tức thì khi có High-Five, bình luận hoặc lời mời kết bạn từ Kafka."
    )
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications() {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return sseNotificationService.subscribe(userId);
    }
}
