package com.nqt.onboarding.interfaceadapters.gateway;

import com.nqt.onboarding.domain.GoalAssessment;
import com.nqt.onboarding.infrastructure.persistence.GoalAssessmentEntity;
import com.nqt.onboarding.usecase.boundary.GoalAssessmentRepositoryGateway;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class GoalAssessmentRepositoryImpl implements GoalAssessmentRepositoryGateway {
    private final GoalAssessmentJpaRepository jpaRepository;

    public GoalAssessmentRepositoryImpl(GoalAssessmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<GoalAssessment> findByGoalId(Long goalId) {
        return jpaRepository.findByGoalId(goalId).map(this::toDomain);
    }

    @Override
    public Optional<GoalAssessment> findByGoalIdAndUserId(Long goalId, Long userId) {
        return jpaRepository.findByGoalIdAndUserId(goalId, userId).map(this::toDomain);
    }

    @Override
    public GoalAssessment save(GoalAssessment assessment) {
        GoalAssessmentEntity entity = toEntity(assessment);
        GoalAssessmentEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private GoalAssessment toDomain(GoalAssessmentEntity entity) {
        if (entity == null) return null;
        return GoalAssessment.builder()
                .id(entity.getId())
                .goalId(entity.getGoalId())
                .userId(entity.getUserId())
                .userEmail(entity.getUserEmail())
                .baseSleepScore(entity.getBaseSleepScore())
                .baseExerciseScore(entity.getBaseExerciseScore())
                .baseStressScore(entity.getBaseStressScore())
                .baseDisciplineScore(entity.getBaseDisciplineScore())
                .baseOverallScore(entity.getBaseOverallScore())
                .postSleepAnswer(entity.getPostSleepAnswer())
                .postSleepScore(entity.getPostSleepScore())
                .postExerciseAnswer(entity.getPostExerciseAnswer())
                .postExerciseScore(entity.getPostExerciseScore())
                .postStressAnswer(entity.getPostStressAnswer())
                .postStressScore(entity.getPostStressScore())
                .postDisciplineAnswer(entity.getPostDisciplineAnswer())
                .postDisciplineScore(entity.getPostDisciplineScore())
                .postOverallScore(entity.getPostOverallScore())
                .deltaScore(entity.getDeltaScore())
                .goalQuestionText(entity.getGoalQuestionText())
                .goalQuestionAnswer(entity.getGoalQuestionAnswer())
                .goalQuestionAnswerLabel(entity.getGoalQuestionAnswerLabel())
                .selfPerceptionPercent(entity.getSelfPerceptionPercent())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private GoalAssessmentEntity toEntity(GoalAssessment domain) {
        if (domain == null) return null;
        return GoalAssessmentEntity.builder()
                .id(domain.getId())
                .goalId(domain.getGoalId())
                .userId(domain.getUserId())
                .userEmail(domain.getUserEmail())
                .baseSleepScore(domain.getBaseSleepScore())
                .baseExerciseScore(domain.getBaseExerciseScore())
                .baseStressScore(domain.getBaseStressScore())
                .baseDisciplineScore(domain.getBaseDisciplineScore())
                .baseOverallScore(domain.getBaseOverallScore())
                .postSleepAnswer(domain.getPostSleepAnswer())
                .postSleepScore(domain.getPostSleepScore())
                .postExerciseAnswer(domain.getPostExerciseAnswer())
                .postExerciseScore(domain.getPostExerciseScore())
                .postStressAnswer(domain.getPostStressAnswer())
                .postStressScore(domain.getPostStressScore())
                .postDisciplineAnswer(domain.getPostDisciplineAnswer())
                .postDisciplineScore(domain.getPostDisciplineScore())
                .postOverallScore(domain.getPostOverallScore())
                .deltaScore(domain.getDeltaScore())
                .goalQuestionText(domain.getGoalQuestionText())
                .goalQuestionAnswer(domain.getGoalQuestionAnswer())
                .goalQuestionAnswerLabel(domain.getGoalQuestionAnswerLabel())
                .selfPerceptionPercent(domain.getSelfPerceptionPercent())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
