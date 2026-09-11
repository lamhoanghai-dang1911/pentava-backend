package com.nqt.onboarding.interfaceadapters.gateway;

import com.nqt.onboarding.domain.UserGoal;
import com.nqt.onboarding.infrastructure.persistence.UserGoalEntity;
import com.nqt.onboarding.usecase.boundary.UserGoalRepositoryGateway;
import org.springframework.stereotype.Repository;
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
    public UserGoal save(UserGoal goal) {
        UserGoalEntity saved = jpaRepository.save(toEntity(goal));
        return toDomain(saved);
    }

    private UserGoal toDomain(UserGoalEntity entity) {
        return UserGoal.builder()
                .id(entity.getId())
                .userId(entity.getUserId()) // ➕ Bổ sung map userId
                .userEmail(entity.getUserEmail())
                .goalName(entity.getGoalName())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    private UserGoalEntity toEntity(UserGoal domain) {
        return UserGoalEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId()) // ➕ Bổ sung map userId
                .userEmail(domain.getUserEmail())
                .goalName(domain.getGoalName())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .completedAt(domain.getCompletedAt())
                .build();
    }
}