package com.nqt.shop.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntitlementCheckResponseDTO {

    private Long userId;

    private String featureCode;

    private boolean hasEntitlement;

    private String paramValue;

    private String displayLabel;

    private String planCode;

    private LocalDateTime expiresAt;
}
