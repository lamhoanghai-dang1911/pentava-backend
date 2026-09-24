package com.nqt.social.infrastructure.persistence.adapter;

import com.nqt.social.domain.Friendship;
import com.nqt.social.domain.FriendshipStatus;
import com.nqt.social.infrastructure.persistence.entity.FriendshipEntity;
import com.nqt.social.infrastructure.persistence.repository.FriendshipJpaRepository;
import com.nqt.social.usecase.boundary.FriendshipRepositoryGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FriendshipRepositoryJpaAdapter implements FriendshipRepositoryGateway {

    private final FriendshipJpaRepository jpaRepository;

    public FriendshipRepositoryJpaAdapter(FriendshipJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Friendship save(Friendship friendship) {
        FriendshipEntity entity = toEntity(friendship);
        FriendshipEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Friendship> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Friendship> findBetween(Long user1Id, Long user2Id) {
        return jpaRepository.findBetween(user1Id, user2Id).map(this::toDomain);
    }

    @Override
    public List<Friendship> findByUserAndStatus(Long userId, FriendshipStatus status) {
        return jpaRepository.findByUserAndStatus(userId, status).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Long> findAcceptedFriendUserIds(Long userId) {
        return jpaRepository.findAcceptedFriendUserIds(userId);
    }

    @Override
    public List<Friendship> findPendingRequestsReceived(Long userId) {
        return jpaRepository.findPendingRequestsReceived(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Friendship> findPendingRequestsSent(Long userId) {
        return jpaRepository.findPendingRequestsSent(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countAcceptedFriends(Long userId) {
        return jpaRepository.countAcceptedFriends(userId);
    }

    @Override
    public List<Long> findAllConnectedUserIds(Long userId) {
        return jpaRepository.findAllConnectedUserIds(userId);
    }

    @Override
    public void delete(Friendship friendship) {
        jpaRepository.deleteById(friendship.getId());
    }

    private Friendship toDomain(FriendshipEntity e) {
        return Friendship.builder()
                .id(e.getId())
                .requesterId(e.getRequesterId())
                .addresseeId(e.getAddresseeId())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private FriendshipEntity toEntity(Friendship d) {
        return FriendshipEntity.builder()
                .id(d.getId())
                .requesterId(d.getRequesterId())
                .addresseeId(d.getAddresseeId())
                .status(d.getStatus())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
