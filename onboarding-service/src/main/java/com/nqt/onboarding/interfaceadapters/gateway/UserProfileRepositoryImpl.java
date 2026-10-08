package com.nqt.onboarding.interfaceadapters.gateway;

import com.nqt.onboarding.domain.UserProfile;
import com.nqt.onboarding.infrastructure.persistence.UserProfileEntity;
import com.nqt.onboarding.usecase.boundary.UserProfileRepositoryGateway;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class UserProfileRepositoryImpl implements UserProfileRepositoryGateway {

    private final UserProfileJpaRepository jpaRepository;

    public UserProfileRepositoryImpl(UserProfileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<UserProfile> findByEmail(String email) {
        return jpaRepository.findByUserEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<UserProfile> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public java.util.List<UserProfile> findByUserIds(java.util.Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return jpaRepository.findByUserIdIn(userIds).stream().map(this::toDomain).toList();
    }

    @Override
    public UserProfile save(UserProfile profile) {
        UserProfileEntity entity = toEntity(profile);
        UserProfileEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private UserProfile toDomain(UserProfileEntity entity) {
        return UserProfile.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .userEmail(entity.getUserEmail())
                .name(entity.getName())
                .gender(entity.getGender())
                .age(entity.getAge())
                .routine(entity.getRoutine())
                .sleepHours(entity.getSleepHours())
                .exerciseFrequency(entity.getExerciseFrequency())
                .stressFrequency(entity.getStressFrequency())
                .screenTime(entity.getScreenTime())
                .habitDuration(entity.getHabitDuration())
                .freeTimes(entity.getFreeTimes())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private UserProfileEntity toEntity(UserProfile domain) {
        return UserProfileEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .userEmail(domain.getUserEmail())
                .name(domain.getName())
                .gender(domain.getGender())
                .age(domain.getAge())
                .routine(domain.getRoutine())
                .sleepHours(domain.getSleepHours())
                .exerciseFrequency(domain.getExerciseFrequency())
                .stressFrequency(domain.getStressFrequency())
                .screenTime(domain.getScreenTime())
                .habitDuration(domain.getHabitDuration())
                .freeTimes(domain.getFreeTimes())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}