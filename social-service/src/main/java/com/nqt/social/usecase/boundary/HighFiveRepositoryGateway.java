package com.nqt.social.usecase.boundary;

import com.nqt.social.domain.HighFive;

import java.util.List;

public interface HighFiveRepositoryGateway {
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    void save(HighFive highFive);
    void deleteByPostIdAndUserId(Long postId, Long userId);
    long countByPostId(Long postId);
    long countTotalHighFivesReceivedByUser(Long userId);
    List<Long> findPostIdsHighFivedByUser(Long userId, List<Long> postIds);
}
