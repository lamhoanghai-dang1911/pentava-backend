package com.nqt.social.infrastructure.persistence.repository;

import com.nqt.social.infrastructure.persistence.entity.HighFiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HighFiveJpaRepository extends JpaRepository<HighFiveEntity, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    @Modifying
    void deleteByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);

    @Query("""
        SELECT COUNT(hf) FROM HighFiveEntity hf
        WHERE hf.postId IN (SELECT p.id FROM PostEntity p WHERE p.userId = :userId)
    """)
    long countTotalHighFivesReceivedByUser(@Param("userId") Long userId);

    @Query("""
        SELECT hf.postId FROM HighFiveEntity hf
        WHERE hf.userId = :userId AND hf.postId IN :postIds
    """)
    List<Long> findPostIdsHighFivedByUser(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);
}
