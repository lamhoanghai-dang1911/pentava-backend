package com.nqt.onboarding.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "goal_id", nullable = false)
    private Long goalId; // FK -> UserGoalEntity.id, tách theo từng chu kỳ 4 tuần

    @Column(name = "goal_name")
    private String goalName;

    @Enumerated(EnumType.STRING)
    @Column(name = "mood_type", nullable = false)
    private MoodType moodType; // BAD, NEUTRAL, GOOD

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "week_number")
    private Integer weekNumber; // 1..4, để hiển thị/audit, KHÔNG dùng để lọc task hôm nay

    @Column(name = "is_ai_generated", nullable = false)
    private Boolean isAiGenerated;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;
}

