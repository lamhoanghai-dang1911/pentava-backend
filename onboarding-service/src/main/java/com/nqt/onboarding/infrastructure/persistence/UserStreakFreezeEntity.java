package com.nqt.onboarding.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_streak_freeze", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "freeze_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStreakFreezeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "freeze_date", nullable = false)
    private LocalDate freezeDate;

    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth; // "YYYY-MM", ví dụ "2026-10"

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
