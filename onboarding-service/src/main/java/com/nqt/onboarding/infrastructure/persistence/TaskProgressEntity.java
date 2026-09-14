package com.nqt.onboarding.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_task_daily", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "task_id", "task_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskProgressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "task_date", nullable = false)
    private LocalDate taskDate;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "current_count", nullable = false)
    @Builder.Default
    private Integer currentCount = 0;

    @Column(name = "target_count", nullable = false)
    @Builder.Default
    private Integer targetCount = 5;

    @Column(name = "selected_items", columnDefinition = "TEXT")
    private String selectedItems;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false; // ➕ mới — xoá mềm khi swap task
}