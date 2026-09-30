package com.nqt.shop.usecase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RubyTopupPricingResponseDTO {

    @Schema(description = "Tỷ lệ quy đổi cơ sở khi nạp lẻ (VNĐ / 1 Ruby)", example = "2000")
    private long baseRateVndPerRuby;

    @Schema(description = "Số tiền nạp lẻ tối thiểu (VNĐ)", example = "20000")
    private long minCustomTopupVnd;

    @Schema(description = "Số Ruby nạp lẻ tối thiểu", example = "10")
    private long minCustomTopupRuby;

    @Schema(description = "Quy tắc nạp lẻ", example = "10 Ruby = 20.000 VNĐ (1 Ruby = 2.000 VNĐ)")
    private String customTopupRule;

    @Schema(description = "Danh sách các gói Ruby ưu đãi")
    private List<RubyPackageDTO> packages;
}
