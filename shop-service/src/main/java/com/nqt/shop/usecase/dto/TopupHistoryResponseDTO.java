package com.nqt.shop.usecase.dto;

import com.nqt.shop.domain.model.TopupStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopupHistoryResponseDTO {

    @Schema(description = "ID bản ghi giao dịch", example = "1")
    private Long id;

    @Schema(description = "Mã giao dịch", example = "PENTAVA849201")
    private String transactionCode;

    @Schema(description = "Số tiền (VNĐ)", example = "100000")
    private long amountVnd;

    @Schema(description = "Số Ruby nhận được", example = "50")
    private long rubyAmount;

    @Schema(description = "Mã gói nạp (SMALL, MEDIUM, LARGE) hoặc CUSTOM nếu nạp lẻ", example = "SMALL")
    private String packageCode;

    @Schema(description = "Tên gói nạp", example = "Small Pack")
    private String packageName;

    @Schema(description = "Trạng thái giao dịch", example = "SUCCESS")
    private TopupStatus status;

    @Schema(description = "Phương thức thanh toán", example = "SEPAY")
    private String paymentMethod;

    @Schema(description = "Thời gian tạo giao dịch")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian hoàn thành giao dịch")
    private LocalDateTime completedAt;
}
