package com.nqt.manage.interfaceadapters.controller;

import com.nqt.manage.usecase.boundary.AdminUserInputPort;
import com.nqt.manage.usecase.dto.AdminUserResponseDTO;
import com.nqt.manage.usecase.dto.UpdateUserNameRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Manage - Users (Admin)", description = "Quản lý và tra cứu người dùng dành cho Quản trị viên (Tên người dùng lấy từ Onboarding-service)")
@RestController
@RequestMapping("/api/manage/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserInputPort adminUserInputPort;

    @Operation(summary = "Xem thông tin người dùng theo User ID (Lấy name từ onboarding-service, auth info từ auth-service)")
    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserResponseDTO> getUserById(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(adminUserInputPort.getUserById(userId));
    }

    @Operation(summary = "Xem thông tin người dùng theo Email (Lấy name từ onboarding-service)")
    @GetMapping("/by-email")
    public ResponseEntity<AdminUserResponseDTO> getUserByEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(adminUserInputPort.getUserByEmail(email));
    }

    @Operation(summary = "Cập nhật tên người dùng (Đồng bộ trực tiếp sang onboarding-service qua gRPC)")
    @PutMapping("/{userId}/name")
    public ResponseEntity<AdminUserResponseDTO> updateUserName(@PathVariable("userId") Long userId,
                                                               @Valid @RequestBody UpdateUserNameRequestDTO requestDTO) {
        return ResponseEntity.ok(adminUserInputPort.updateUserName(userId, requestDTO));
    }
}
