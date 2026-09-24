package com.nqt.social.infrastructure.persistence.entity;

import com.nqt.social.domain.PrivacyMode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_posts_user_id", columnList = "user_id"),
        @Index(name = "idx_posts_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "clip_id", nullable = false)
    private Long clipId;

    @Column(name = "goal_id")
    private Long goalId;

    @Column(name = "week_number")
    private Integer weekNumber;

    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;

    @Column(columnDefinition = "TEXT")
    private String caption;

    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy_mode", nullable = false)
    @Builder.Default
    private PrivacyMode privacyMode = PrivacyMode.PUBLIC;

    @Builder.Default
    @Column(name = "high_five_count", nullable = false)
    private long highFiveCount = 0;

    @Builder.Default
    @Column(name = "comment_count", nullable = false)
    private long commentCount = 0;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
