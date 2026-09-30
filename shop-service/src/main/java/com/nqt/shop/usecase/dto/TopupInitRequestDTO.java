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
public class TopupInitRequestDTO {

    @Schema(description = "Mã gói nạp (SMALL, MEDIUM, LARGE) nếu mua gói Ruby. Để trống nếu nạp lẻ.", example = "SMALL")
    private String packageCode;

    @Schema(description = "Số tiền VNĐ khi nạp lẻ (Tối thiểu 20.000 VNĐ = 10 Ruby, phải là bội số của 2.000 VNĐ). Bỏ trống nếu chọn packageCode.", example = "20000")
    private Long amountVnd;

    @Schema(description = "Số Ruby khi nạp lẻ (Tối thiểu 10 Ruby). Bỏ trống nếu đã nhập amountVnd hoặc chọn packageCode.", example = "10")
    private Long rubyAmount;
}
