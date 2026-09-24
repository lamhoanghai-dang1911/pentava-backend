package com.nqt.social.infrastructure.persistence.repository;

import com.nqt.social.domain.FriendshipStatus;
import com.nqt.social.infrastructure.persistence.entity.FriendshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipJpaRepository extends JpaRepository<FriendshipEntity, Long> {

    @Query("""
        SELECT f FROM FriendshipEntity f
        WHERE (f.requesterId = :u1 AND f.addresseeId = :u2)
           OR (f.requesterId = :u2 AND f.addresseeId = :u1)
    """)
    Optional<FriendshipEntity> findBetween(@Param("u1") Long u1, @Param("u2") Long u2);

    @Query("""
        SELECT f FROM FriendshipEntity f
        WHERE (f.requesterId = :userId OR f.addresseeId = :userId)
          AND f.status = :status
        ORDER BY f.updatedAt DESC
    """)
    List<FriendshipEntity> findByUserAndStatus(@Param("userId") Long userId, @Param("status") FriendshipStatus status);

    @Query("""
        SELECT CASE WHEN f.requesterId = :userId THEN f.addresseeId ELSE f.requesterId END
        FROM FriendshipEntity f
        WHERE (f.requesterId = :userId OR f.addresseeId = :userId)
          AND f.status = com.nqt.social.domain.FriendshipStatus.ACCEPTED
    """)
    List<Long> findAcceptedFriendUserIds(@Param("userId") Long userId);

    @Query("""
        SELECT f FROM FriendshipEntity f
        WHERE f.addresseeId = :userId AND f.status = com.nqt.social.domain.FriendshipStatus.PENDING
        ORDER BY f.createdAt DESC
    """)
    List<FriendshipEntity> findPendingRequestsReceived(@Param("userId") Long userId);

    @Query("""
        SELECT f FROM FriendshipEntity f
        WHERE f.requesterId = :userId AND f.status = com.nqt.social.domain.FriendshipStatus.PENDING
        ORDER BY f.createdAt DESC
    """)
    List<FriendshipEntity> findPendingRequestsSent(@Param("userId") Long userId);

    @Query("""
        SELECT COUNT(f) FROM FriendshipEntity f
        WHERE (f.requesterId = :userId OR f.addresseeId = :userId)
          AND f.status = com.nqt.social.domain.FriendshipStatus.ACCEPTED
    """)
    long countAcceptedFriends(@Param("userId") Long userId);

    @Query("""
        SELECT DISTINCT CASE WHEN f.requesterId = :userId THEN f.addresseeId ELSE f.requesterId END
        FROM FriendshipEntity f
        WHERE f.requesterId = :userId OR f.addresseeId = :userId
    """)
    List<Long> findAllConnectedUserIds(@Param("userId") Long userId);
}
