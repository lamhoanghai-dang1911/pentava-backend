package com.nqt.manage.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteConfig {
    private Long id;
    private String configKey;
    private String configValue;
    private String groupName;
    private String description;
    private Boolean isPublic;
    private LocalDateTime updatedAt;
}
