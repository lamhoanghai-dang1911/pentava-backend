package com.nqt.manage.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteConfigResponseDTO {
    private Long id;
    private String configKey;
    private String configValue;
    private String groupName;
    private String description;
    private Boolean isPublic;
    private LocalDateTime updatedAt;
}
