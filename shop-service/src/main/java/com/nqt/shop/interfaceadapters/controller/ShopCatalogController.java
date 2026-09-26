package com.nqt.shop.interfaceadapters.controller;

import com.nqt.shop.infrastructure.security.CurrentUserProvider;
import com.nqt.shop.usecase.boundary.ShopCatalogInputPort;
import com.nqt.shop.usecase.boundary.ShopPurchaseInputPort;
import com.nqt.shop.usecase.dto.BuyItemResponseDTO;
import com.nqt.shop.usecase.dto.ShopItemResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Shop - Catalog & Purchase", description = "Cửa hàng bán skin trang phục nhân vật, kiểm tra trạng thái sở hữu và mua bằng Ruby")
@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopCatalogController {

    private final ShopCatalogInputPort shopCatalogInputPort;
    private final ShopPurchaseInputPort shopPurchaseInputPort;
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "Xem danh sách skin đang bán trong cửa hàng (tự động hiển thị trạng thái Đã sở hữu hay Chưa sở hữu)")
    @GetMapping("/items")
    public ResponseEntity<List<ShopItemResponseDTO>> getCatalog() {
        Long userId = currentUserProvider.getOptionalCurrentUserId().orElse(null);
        return ResponseEntity.ok(shopCatalogInputPort.getCatalog(userId));
    }

    @Operation(summary = "Mua một skin trong cửa hàng bằng Ruby (trừ Ruby và tự động cấp skin vào kho đồ nhân vật)")
    @PostMapping("/items/{skinId}/buy")
    public ResponseEntity<BuyItemResponseDTO> buyItem(
            @io.swagger.v3.oas.annotations.Parameter(description = "ID của skin muốn mua", required = true, example = "2")
            @PathVariable("skinId") Long skinId) {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(shopPurchaseInputPort.buyItem(userId, skinId));
    }
}
