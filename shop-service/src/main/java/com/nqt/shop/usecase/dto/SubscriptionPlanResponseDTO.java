package com.nqt.shop.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanResponseDTO {
    private Long id;
    private String code;
    private String name;
    private long priceVnd;
    private int durationDays;
    private String description;
    private String badge;
    private boolean isActive;
    private int displayOrder;
    private List<PlanFeatureItemDTO> features;
}
