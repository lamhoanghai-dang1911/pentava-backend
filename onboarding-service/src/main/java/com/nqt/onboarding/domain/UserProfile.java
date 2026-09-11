package com.nqt.onboarding.domain;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserProfile {
    private Long id;
    private Long userId;
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