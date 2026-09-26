package com.nqt.skin.interfaceadapters.controller;

import com.nqt.skin.infrastructure.security.CurrentUserProvider;
import com.nqt.skin.usecase.boundary.SkinInputPort;
import com.nqt.skin.usecase.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Skin Service", description = "Quản lý skin nhân vật, trang bị và render thứ tự các layer")
@RestController
@RequestMapping("/api/skin")
@RequiredArgsConstructor
public class SkinController {

    private final SkinInputPort skinInputPort;
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "Lấy các layer hiển thị avatar của chính mình (sắp xếp từ layer 0 cat_base lên các layer trang bị)")
    @GetMapping("/avatar/my-avatar")
    public ResponseEntity<AvatarRenderResponseDTO> getMyAvatar() {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(skinInputPort.getAvatar(userId));
    }

    @Operation(summary = "Lấy các layer hiển thị avatar của một user bất kỳ theo userId")
    @GetMapping("/avatar/users/{userId}")
    public ResponseEntity<AvatarRenderResponseDTO> getUserAvatar(
            @io.swagger.v3.oas.annotations.Parameter(description = "ID của user cần xem avatar", required = true, example = "1")
            @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(skinInputPort.getAvatar(userId));
    }

    @Operation(summary = "Lấy danh sách tất cả các skin đang có trong hệ thống (công khai)")
    @GetMapping("/items")
    public ResponseEntity<List<SkinItemResponseDTO>> getAllSkins() {
        return ResponseEntity.ok(skinInputPort.getAllSkins());
    }

    @Operation(summary = "Xem kho đồ cá nhân (danh sách skin đã sở hữu và trạng thái đang trang bị)")
    @GetMapping("/inventory")
    public ResponseEntity<List<SkinItemResponseDTO>> getMyInventory() {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(skinInputPort.getUserInventory(userId));
    }

    @Operation(summary = "Trang bị skin vào slot tương ứng (tự động thay thế nếu slot đã có đồ)")
    @PostMapping("/equip")
    public ResponseEntity<AvatarRenderResponseDTO> equipSkin(@Valid @RequestBody EquipSkinRequestDTO dto) {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(skinInputPort.equipSkin(userId, dto.getSkinId()));
    }

    @Operation(summary = "Tháo skin ra khỏi slot chỉ định (HEAD, EYES, NECK, ACCESSORY)")
    @PostMapping("/unequip")
    public ResponseEntity<AvatarRenderResponseDTO> unequipSkin(@Valid @RequestBody UnequipSkinRequestDTO dto) {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(skinInputPort.unequipSkin(userId, dto.getSlot()));
    }
}
