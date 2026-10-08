package com.nqt.shop.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAllEntitlementsResponseDTO {

    private Long userId;

    private boolean hasActiveSubscription;

    private String planCode;

    private String planName;

    private LocalDateTime expiresAt;

    private List<PlanFeatureItemDTO> entitlements;
}
