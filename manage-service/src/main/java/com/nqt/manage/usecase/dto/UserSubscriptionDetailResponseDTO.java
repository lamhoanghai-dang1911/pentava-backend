package com.nqt.manage.usecase.dto;

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
public class UserSubscriptionDetailResponseDTO {

    private Long userId;

    private boolean hasActiveSubscription;

    private String planCode;

    private String planName;

    private String badge;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private long daysRemaining;

    private List<PlanFeatureItemDTO> activeFeatures;
}
