package com.nqt.social.interfaceadapters.controller;

import com.nqt.social.infrastructure.security.CurrentUserProvider;
import com.nqt.social.usecase.boundary.ProfileInputPort;
import com.nqt.social.usecase.dto.UserProfileResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Social - Profile", description = "Trang cá nhân người dùng (tổng hợp auth-service qua gRPC và social stats)")
@RestController
@RequestMapping("/api/social/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileInputPort profileInputPort;
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "Xem trang cá nhân của một người dùng")
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponseDTO> getProfile(@PathVariable("userId") Long targetUserId) {
        Long viewerId = currentUserProvider.getOptionalCurrentUserId().orElse(null);
        return ResponseEntity.ok(profileInputPort.getProfile(targetUserId, viewerId));
    }

    @Operation(summary = "Xem trang cá nhân của chính mình")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> getMyProfile() {
        Long myId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(profileInputPort.getProfile(myId, myId));
    }
}
