package com.nqt.onboarding.infrastructure.persistence;

import com.nqt.onboarding.domain.GoalStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGoalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String goalName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalStatus status;

    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}