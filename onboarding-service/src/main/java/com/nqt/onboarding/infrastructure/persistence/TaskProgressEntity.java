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
    private Boolean isCompleted = false;

    // ➕ Thêm cột này để lưu các item user tích chọn (Lưu dạng chuỗi ngăn cách bằng dấu phẩy, ví dụ: "Thức ăn nhanh,Nước ngọt có ga")
    @Column(name = "selected_items", columnDefinition = "TEXT")
    private String selectedItems;
}