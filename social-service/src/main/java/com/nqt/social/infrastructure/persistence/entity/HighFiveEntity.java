package com.nqt.social.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "high_fives",
        uniqueConstraints = @UniqueConstraint(name = "uk_post_user_high_five", columnNames = {"post_id", "user_id"}),
        indexes = {
                @Index(name = "idx_high_five_post", columnList = "post_id"),
                @Index(name = "idx_high_five_user", columnList = "user_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HighFiveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
