package com.nqt.shop.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyItemResponseDTO {
    private boolean success;
    private String message;
    private Long skinItemId;
    private String skinName;
    private int rubyDeducted;
    private long remainingRuby;
}
