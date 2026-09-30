package com.nqt.shop.usecase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RubyPackageDTO {

    @Schema(description = "Mã định danh gói nạp", example = "SMALL")
    private String code;

    @Schema(description = "Tên gói nạp", example = "Small Pack")
    private String name;

    @Schema(description = "Số lượng Ruby nhận được", example = "50")
    private long rubyAmount;

    @Schema(description = "Giá tiền thanh toán (VNĐ)", example = "100000")
    private long priceVnd;

    @Schema(description = "Giá gốc nếu tính theo giá nạp lẻ (VNĐ)", example = "100000")
    private long originalPriceVnd;

    @Schema(description = "Số tiền tiết kiệm được so với nạp lẻ (VNĐ)", example = "0")
    private long discountVnd;

    @Schema(description = "Số Ruby được tặng thêm so với nạp lẻ", example = "0")
    private long bonusRuby;

    @Schema(description = "Mô tả chi tiết gói nạp", example = "Small Pack (50 ruby) = 100.000 VNĐ")
    private String description;

    @Schema(description = "Đánh dấu gói nạp phổ biến nhất", example = "false")
    private boolean isPopular;

    @Schema(description = "Đánh dấu gói nạp có giá trị ưu đãi tốt nhất", example = "false")
    private boolean isBestValue;
}
