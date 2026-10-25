package com.nqt.shop.interfaceadapters.controller;

import com.nqt.shop.infrastructure.realtime.CashflowRealtimeService;
import com.nqt.shop.usecase.boundary.CashflowInputPort;
import com.nqt.shop.usecase.dto.CashflowRecordResponseDTO;
import com.nqt.shop.usecase.dto.CashflowSummaryResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@Tag(name = "Shop - Admin Cashflow (Quản trị Dòng tiền & Sổ quỹ Realtime)", description = "APIs dành cho Admin để giám sát dòng tiền vào/ra, sổ quỹ giao dịch và lắng nghe luồng sự kiện realtime khi có user thanh toán")
@RestController
@RequestMapping("/api/shop/admin/cashflow")
@RequiredArgsConstructor
public class ShopAdminCashflowController {

    private final CashflowInputPort cashflowInputPort;
    private final CashflowRealtimeService cashflowRealtimeService;

    @Operation(summary = "Lắng nghe luồng dữ liệu thanh toán thời gian thực (Server-Sent Events)")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeCashflowStream() {
        log.info("Admin mở kết nối SSE tới luồng dòng tiền realtime.");
        return cashflowRealtimeService.registerClient();
    }

    @Operation(summary = "Lấy danh sách lịch sử giao dịch dòng tiền kèm bộ lọc chi tiết")
    @GetMapping("/transactions")
    public ResponseEntity<List<CashflowRecordResponseDTO>> getTransactions(
            @RequestParam(value = "direction", required = false, defaultValue = "ALL") String direction,
            @RequestParam(value = "type", required = false, defaultValue = "ALL") String type,
            @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "dateRange", required = false, defaultValue = "ALL") String dateRange) {
        return ResponseEntity.ok(cashflowInputPort.getTransactions(direction, type, status, searchTerm, dateRange));
    }

    @Operation(summary = "Lấy báo cáo tổng hợp sổ quỹ & các chỉ số KPI tài chính dòng tiền")
    @GetMapping("/summary")
    public ResponseEntity<CashflowSummaryResponseDTO> getCashflowSummary() {
        return ResponseEntity.ok(cashflowInputPort.getCashflowSummary());
    }

    @Operation(summary = "Duyệt thủ công hoàn tất giao dịch đang chờ (Topup hoặc Gói cước)")
    @PostMapping("/approve/{transactionCode}")
    public ResponseEntity<CashflowRecordResponseDTO> approveTransaction(
            @PathVariable("transactionCode") String transactionCode) {
        log.info("Admin yêu cầu duyệt thủ công giao dịch: {}", transactionCode);
        return ResponseEntity.ok(cashflowInputPort.approveTransaction(transactionCode));
    }
}
