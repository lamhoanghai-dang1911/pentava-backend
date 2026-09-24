package com.nqt.social.infrastructure.persistence.repository;

import com.nqt.social.domain.PrivacyMode;
import com.nqt.social.infrastructure.persistence.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostJpaRepository extends JpaRepository<PostEntity, Long> {

    List<PostEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PostEntity> findByUserIdAndPrivacyModeInOrderByCreatedAtDesc(Long userId, List<PrivacyMode> privacyModes);

    long countByUserId(Long userId);

    @Query("""
        SELECT p FROM PostEntity p
        WHERE p.privacyMode = 'PUBLIC'
           OR (p.privacyMode = 'FRIENDS_ONLY' AND p.userId IN :friendIds)
           OR (:viewerId IS NOT NULL AND p.userId = :viewerId)
        ORDER BY p.createdAt DESC
    """)
    List<PostEntity> findAllAccessiblePosts(
            @Param("viewerId") Long viewerId,
            @Param("friendIds") List<Long> friendIds
    );

    @Query("""
        SELECT p FROM PostEntity p
        WHERE p.userId IN :friendIds
          AND p.privacyMode IN ('PUBLIC', 'FRIENDS_ONLY')
        ORDER BY p.createdAt DESC
    """)
    List<PostEntity> findFriendsPosts(@Param("friendIds") List<Long> friendIds);

    @Modifying
    @Query("UPDATE PostEntity p SET p.highFiveCount = :hfCount, p.commentCount = :cCount WHERE p.id = :postId")
    void updateCounters(@Param("postId") Long postId, @Param("hfCount") long hfCount, @Param("cCount") long cCount);
}
