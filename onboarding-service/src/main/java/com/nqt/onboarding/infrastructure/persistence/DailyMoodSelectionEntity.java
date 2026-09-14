package com.nqt.onboarding.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_mood_selection", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "goal_id", "selection_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyMoodSelectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "goal_id", nullable = false)
    private Long goalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mood_type", nullable = false)
    private MoodType moodType;

    @Column(name = "selection_date", nullable = false)
    private LocalDate selectionDate;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment; // ô "Comment...." trong UI
}
