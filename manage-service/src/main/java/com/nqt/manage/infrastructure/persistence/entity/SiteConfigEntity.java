package com.nqt.manage.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "landing_site_configs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String configKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String configValue;

    private String groupName;

    private String description;

    @Builder.Default
    private Boolean isPublic = true;

    private LocalDateTime updatedAt;
}
