package com.nqt.onboarding.interfaceadapters.gateway;

import com.nqt.onboarding.domain.GoalStatus;
import com.nqt.onboarding.domain.UserGoal;
import com.nqt.onboarding.infrastructure.persistence.UserGoalEntity;
import com.nqt.onboarding.usecase.boundary.UserGoalRepositoryGateway;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserGoalRepositoryImpl implements UserGoalRepositoryGateway {
    private final UserGoalJpaRepository jpaRepository;

    public UserGoalRepositoryImpl(UserGoalJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<UserGoal> findByEmailAndGoalName(String email, String goalName) {
        return jpaRepository.findByUserEmailAndGoalName(email, goalName).map(this::toDomain);
    }

    @Override
    public Optional<UserGoal> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<UserGoal> findByUserIdAndStatus(Long userId, GoalStatus status) {
        return jpaRepository.findByUserIdAndStatus(userId, status).map(this::toDomain);
    }

    @Override
    public List<UserGoal> findAllByUserId(Long userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<UserGoal> findPublicGoals() {
        return jpaRepository.findByIsPublicTrueOrderByCreatedAtDesc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public UserGoal save(UserGoal goal) {
        UserGoalEntity saved = jpaRepository.save(toEntity(goal));
        return toDomain(saved);
    }

    private UserGoal toDomain(UserGoalEntity entity) {
        return UserGoal.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .userEmail(entity.getUserEmail())
                .goalName(entity.getGoalName())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isCustom(entity.getIsCustom())
                .isPublic(entity.getIsPublic())
                .description(entity.getDescription())
                .authorName(entity.getAuthorName())
                .build();
    }

    private UserGoalEntity toEntity(UserGoal domain) {
        return UserGoalEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .userEmail(domain.getUserEmail())
                .goalName(domain.getGoalName())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .completedAt(domain.getCompletedAt())
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .isCustom(domain.getIsCustom())
                .isPublic(domain.getIsPublic())
                .description(domain.getDescription())
                .authorName(domain.getAuthorName())
                .build();
    }
}