package com.nqt.shop.usecase.dto;

import com.nqt.shop.domain.model.TopupStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopupInitResponseDTO {

    @Schema(description = "Mã giao dịch chuyển khoản SePay", example = "PENTAVA849201")
    private String transactionCode;

    @Schema(description = "Số tiền cần chuyển khoản (VNĐ)", example = "100000")
    private long amountVnd;

    @Schema(description = "Số Ruby sẽ nhận được khi nạp thành công", example = "50")
    private long rubyAmount;

    @Schema(description = "Mã gói nạp (SMALL, MEDIUM, LARGE) hoặc CUSTOM nếu nạp lẻ", example = "SMALL")
    private String packageCode;

    @Schema(description = "Tên gói nạp hoặc Nạp lẻ", example = "Small Pack")
    private String packageName;

    @Schema(description = "Mô tả chi tiết giao dịch", example = "Small Pack (50 ruby) = 100.000 VNĐ")
    private String description;

    @Schema(description = "Ngân hàng thụ hưởng", example = "MBBank")
    private String bankCode;

    @Schema(description = "Số tài khoản ngân hàng thụ hưởng", example = "0123456789")
    private String accountNumber;

    @Schema(description = "Tên chủ tài khoản thụ hưởng", example = "PENTAVA")
    private String accountName;

    @Schema(description = "Nội dung chuyển khoản cần ghi chính xác", example = "PENTAVA849201")
    private String transferContent;

    @Schema(description = "Đường dẫn ảnh mã QR SePay / VietQR", example = "https://qr.sepay.vn/img?...")
    private String qrCodeUrl;

    @Schema(description = "Trạng thái giao dịch", example = "PENDING")
    private TopupStatus status;
}
