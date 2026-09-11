package com.nqt.onboarding.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private String userEmail;

    private String name;
    private String gender;
    private String age;
    private String routine;
    private String sleepHours;
    private String exerciseFrequency;
    private String stressFrequency;
    private String screenTime;
    private String habitDuration;
    private String freeTimes;
    private LocalDateTime createdAt;
}