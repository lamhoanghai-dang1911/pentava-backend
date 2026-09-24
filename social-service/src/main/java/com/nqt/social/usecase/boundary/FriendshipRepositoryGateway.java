package com.nqt.social.usecase.boundary;

import com.nqt.social.domain.Friendship;
import com.nqt.social.domain.FriendshipStatus;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepositoryGateway {
    Friendship save(Friendship friendship);
    Optional<Friendship> findById(Long id);
    Optional<Friendship> findBetween(Long user1Id, Long user2Id);
    List<Friendship> findByUserAndStatus(Long userId, FriendshipStatus status);
    List<Long> findAcceptedFriendUserIds(Long userId);
    List<Friendship> findPendingRequestsReceived(Long userId);
    List<Friendship> findPendingRequestsSent(Long userId);
    long countAcceptedFriends(Long userId);
    List<Long> findAllConnectedUserIds(Long userId);
    void delete(Friendship friendship);
}
