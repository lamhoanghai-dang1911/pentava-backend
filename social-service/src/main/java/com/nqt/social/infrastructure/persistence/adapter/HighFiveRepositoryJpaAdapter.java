package com.nqt.social.infrastructure.persistence.adapter;

import com.nqt.social.domain.HighFive;
import com.nqt.social.infrastructure.persistence.entity.HighFiveEntity;
import com.nqt.social.infrastructure.persistence.repository.HighFiveJpaRepository;
import com.nqt.social.usecase.boundary.HighFiveRepositoryGateway;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class HighFiveRepositoryJpaAdapter implements HighFiveRepositoryGateway {

    private final HighFiveJpaRepository jpaRepository;

    public HighFiveRepositoryJpaAdapter(HighFiveJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByPostIdAndUserId(Long postId, Long userId) {
        return jpaRepository.existsByPostIdAndUserId(postId, userId);
    }

    @Override
    public void save(HighFive highFive) {
        HighFiveEntity entity = HighFiveEntity.builder()
                .postId(highFive.getPostId())
                .userId(highFive.getUserId())
                .createdAt(highFive.getCreatedAt())
                .build();
        jpaRepository.save(entity);
    }

    @Override
    public void deleteByPostIdAndUserId(Long postId, Long userId) {
        jpaRepository.deleteByPostIdAndUserId(postId, userId);
    }

    @Override
    public long countByPostId(Long postId) {
        return jpaRepository.countByPostId(postId);
    }

    @Override
    public long countTotalHighFivesReceivedByUser(Long userId) {
        return jpaRepository.countTotalHighFivesReceivedByUser(userId);
    }

    @Override
    public List<Long> findPostIdsHighFivedByUser(Long userId, List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyList();
        }
        return jpaRepository.findPostIdsHighFivedByUser(userId, postIds);
    }
}
