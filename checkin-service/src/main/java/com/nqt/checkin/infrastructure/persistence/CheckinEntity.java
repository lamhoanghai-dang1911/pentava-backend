package com.nqt.checkin.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "checkins", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "task_progress_id", "checkin_date"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckinEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "task_progress_id", nullable = false)
    private Long taskProgressId;

    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}