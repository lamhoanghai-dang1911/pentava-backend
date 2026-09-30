package com.nqt.manage.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "landing_testimonials")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestimonialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String authorName;

    private String authorTitle;

    @Column(length = 1000)
    private String avatarUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    private Integer rating = 5;

    private Long userId;

    @Builder.Default
    private Boolean isFeatured = false;

    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
