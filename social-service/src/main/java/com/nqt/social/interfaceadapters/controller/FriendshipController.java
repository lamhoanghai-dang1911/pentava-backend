package com.nqt.social.interfaceadapters.controller;

import com.nqt.social.infrastructure.security.CurrentUserProvider;
import com.nqt.social.usecase.boundary.FriendshipInputPort;
import com.nqt.social.usecase.dto.FriendSuggestionDTO;
import com.nqt.social.usecase.dto.FriendshipDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Social - Friends", description = "Quản lý kết bạn và danh sách bạn bè")
@RestController
@RequestMapping("/api/social/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipInputPort friendshipInputPort;
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "Đề xuất danh sách kết bạn (People You May Know)",
               description = "Đề xuất người dùng dựa trên bạn chung, độ hoạt động (số clip đã đăng tải) hoặc thành viên mới.")
    @GetMapping("/suggestions")
    public ResponseEntity<List<FriendSuggestionDTO>> getFriendSuggestions(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(friendshipInputPort.getFriendSuggestions(userId, limit));
    }

    @Operation(summary = "Gửi lời mời kết bạn")
    @PostMapping("/request/{targetUserId}")
    public ResponseEntity<FriendshipDTO> sendFriendRequest(@PathVariable("targetUserId") Long targetUserId) {
        Long requesterId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(friendshipInputPort.sendFriendRequest(requesterId, targetUserId));
    }

    @Operation(summary = "Chấp nhận lời mời kết bạn")
    @PostMapping("/accept/{friendshipId}")
    public ResponseEntity<FriendshipDTO> acceptFriendRequest(@PathVariable("friendshipId") Long friendshipId) {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(friendshipInputPort.acceptFriendRequest(friendshipId, userId));
    }

    @Operation(summary = "Từ chối lời mời kết bạn")
    @PostMapping("/reject/{friendshipId}")
    public ResponseEntity<Void> rejectFriendRequest(@PathVariable("friendshipId") Long friendshipId) {
        Long userId = currentUserProvider.resolveCurrentUserId();
        friendshipInputPort.rejectFriendRequest(friendshipId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Huỷ kết bạn")
    @DeleteMapping("/{friendUserId}")
    public ResponseEntity<Void> unfriend(@PathVariable("friendUserId") Long friendUserId) {
        Long userId = currentUserProvider.resolveCurrentUserId();
        friendshipInputPort.unfriend(friendUserId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Xem danh sách bạn bè")
    @GetMapping
    public ResponseEntity<List<FriendshipDTO>> getFriends() {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(friendshipInputPort.getFriends(userId));
    }

    @Operation(summary = "Xem danh sách lời mời kết bạn chờ duyệt")
    @GetMapping("/requests")
    public ResponseEntity<List<FriendshipDTO>> getPendingRequests() {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(friendshipInputPort.getPendingRequests(userId));
    }
}
