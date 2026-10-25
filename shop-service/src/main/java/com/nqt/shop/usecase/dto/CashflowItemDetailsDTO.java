package com.nqt.shop.usecase.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CashflowItemDetailsDTO {
    private String itemName;
    private String itemCode;
    private String slot;
    private String planCode;
    private Integer durationDays;
    private Long bonusRuby;
}
