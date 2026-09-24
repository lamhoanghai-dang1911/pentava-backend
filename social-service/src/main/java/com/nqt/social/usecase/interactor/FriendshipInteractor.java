package com.nqt.social.usecase.interactor;

import com.nqt.social.domain.Friendship;
import com.nqt.social.domain.FriendshipStatus;
import com.nqt.social.domain.SocialEvent;
import com.nqt.social.usecase.boundary.AuthServiceGateway;
import com.nqt.social.usecase.boundary.FriendshipInputPort;
import com.nqt.social.usecase.boundary.FriendshipRepositoryGateway;
import com.nqt.social.usecase.boundary.PostRepositoryGateway;
import com.nqt.social.usecase.boundary.SocialEventPublisherGateway;
import com.nqt.social.usecase.dto.FriendshipDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FriendshipInteractor implements FriendshipInputPort {

    private final FriendshipRepositoryGateway friendshipRepositoryGateway;
    private final AuthServiceGateway authServiceGateway;
    private final SocialEventPublisherGateway socialEventPublisherGateway;
    private final PostRepositoryGateway postRepositoryGateway;

    public FriendshipInteractor(
            FriendshipRepositoryGateway friendshipRepositoryGateway,
            AuthServiceGateway authServiceGateway,
            SocialEventPublisherGateway socialEventPublisherGateway,
            PostRepositoryGateway postRepositoryGateway) {
        this.friendshipRepositoryGateway = friendshipRepositoryGateway;
        this.authServiceGateway = authServiceGateway;
        this.socialEventPublisherGateway = socialEventPublisherGateway;
        this.postRepositoryGateway = postRepositoryGateway;
    }

    @Override
    @Transactional
    public FriendshipDTO sendFriendRequest(Long requesterId, Long targetUserId) {
        if (requesterId.equals(targetUserId)) {
            throw new IllegalArgumentException("Không thể gửi lời mời kết bạn cho chính mình");
        }

        // Kiểm tra user có tồn tại ở auth-service không
        authServiceGateway.getUserById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + targetUserId));

        Optional<Friendship> existingOpt = friendshipRepositoryGateway.findBetween(requesterId, targetUserId);
        Friendship friendship;

        if (existingOpt.isPresent()) {
            Friendship existing = existingOpt.get();
            if (existing.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new IllegalStateException("Hai bạn đã là bạn bè");
            }
            if (existing.getStatus() == FriendshipStatus.PENDING) {
                throw new IllegalStateException("Lời mời kết bạn đang chờ phản hồi");
            }
            // Nếu đã từng từ chối, gửi lại lời mời
            existing.setRequesterId(requesterId);
            existing.setAddresseeId(targetUserId);
            existing.setStatus(FriendshipStatus.PENDING);
            existing.setUpdatedAt(LocalDateTime.now());
            friendship = friendshipRepositoryGateway.save(existing);
        } else {
            friendship = Friendship.builder()
                    .requesterId(requesterId)
                    .addresseeId(targetUserId)
                    .status(FriendshipStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            friendship = friendshipRepositoryGateway.save(friendship);
        }

        // Gửi event Kafka
        AuthServiceGateway.UserSocialInfo requester = authServiceGateway.getUserById(requesterId).orElse(null);
        SocialEvent event = SocialEvent.builder()
                .eventType(SocialEvent.EventType.FRIEND_REQUEST)
                .actorId(requesterId)
                .actorName(requester != null ? requester.name() : "Người dùng")
                .actorAvatar(requester != null ? requester.avatarUrl() : "")
                .targetUserId(targetUserId)
                .message((requester != null ? requester.name() : "Ai đó") + " đã gửi cho bạn một lời mời kết bạn!")
                .timestamp(LocalDateTime.now())
                .build();

        socialEventPublisherGateway.publish(event);

        AuthServiceGateway.UserSocialInfo targetUser = authServiceGateway.getUserById(targetUserId).orElse(null);
        return toDto(friendship, targetUserId, targetUser);
    }

    @Override
    @Transactional
    public FriendshipDTO acceptFriendRequest(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepositoryGateway.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lời mời kết bạn"));

        if (!friendship.getAddresseeId().equals(userId)) {
            throw new IllegalStateException("Bạn không phải người nhận lời mời kết bạn này");
        }

        if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
            throw new IllegalStateException("Lời mời này đã được chấp nhận trước đó");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setUpdatedAt(LocalDateTime.now());
        Friendship saved = friendshipRepositoryGateway.save(friendship);

        // Gửi event Kafka cho người gửi lời mời
        AuthServiceGateway.UserSocialInfo accepter = authServiceGateway.getUserById(userId).orElse(null);
        SocialEvent event = SocialEvent.builder()
                .eventType(SocialEvent.EventType.FRIEND_ACCEPTED)
                .actorId(userId)
                .actorName(accepter != null ? accepter.name() : "Người dùng")
                .actorAvatar(accepter != null ? accepter.avatarUrl() : "")
                .targetUserId(friendship.getRequesterId())
                .message((accepter != null ? accepter.name() : "Ai đó") + " đã chấp nhận lời mời kết bạn của bạn!")
                .timestamp(LocalDateTime.now())
                .build();

        socialEventPublisherGateway.publish(event);

        AuthServiceGateway.UserSocialInfo requester = authServiceGateway.getUserById(friendship.getRequesterId()).orElse(null);
        return toDto(saved, friendship.getRequesterId(), requester);
    }

    @Override
    @Transactional
    public void rejectFriendRequest(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepositoryGateway.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lời mời kết bạn"));

        if (!friendship.getAddresseeId().equals(userId)) {
            throw new IllegalStateException("Bạn không phải người nhận lời mời kết bạn này");
        }

        friendshipRepositoryGateway.delete(friendship);
    }

    @Override
    @Transactional
    public void unfriend(Long friendUserId, Long userId) {
        Friendship friendship = friendshipRepositoryGateway.findBetween(userId, friendUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mối quan hệ bạn bè"));

        friendshipRepositoryGateway.delete(friendship);
    }

    @Override
    public List<FriendshipDTO> getFriends(Long userId) {
        List<Friendship> friendships = friendshipRepositoryGateway.findByUserAndStatus(userId, FriendshipStatus.ACCEPTED);

        return friendships.stream()
                .map(f -> {
                    Long otherUserId = f.getRequesterId().equals(userId) ? f.getAddresseeId() : f.getRequesterId();
                    AuthServiceGateway.UserSocialInfo otherUser = authServiceGateway.getUserById(otherUserId).orElse(null);
                    return toDto(f, otherUserId, otherUser);
                })
                .toList();
    }

    @Override
    public List<FriendshipDTO> getPendingRequests(Long userId) {
        List<Friendship> pendingList = friendshipRepositoryGateway.findPendingRequestsReceived(userId);

        return pendingList.stream()
                .map(f -> {
                    AuthServiceGateway.UserSocialInfo requester = authServiceGateway.getUserById(f.getRequesterId()).orElse(null);
                    return toDto(f, f.getRequesterId(), requester);
                })
                .toList();
    }

    @Override
    public List<com.nqt.social.usecase.dto.FriendSuggestionDTO> getFriendSuggestions(Long userId, int limit) {
        int maxSuggestions = Math.max(1, Math.min(limit, 50));

        // 1. Lấy danh sách tất cả user đã có liên kết với userId (đã kết bạn, chờ duyệt, bị chặn)
        List<Long> connectedIds = new java.util.ArrayList<>(friendshipRepositoryGateway.findAllConnectedUserIds(userId));
        connectedIds.add(userId);

        // 2. Lấy danh sách bạn bè hiện tại của userId
        java.util.Set<Long> myFriendIds = new java.util.HashSet<>(friendshipRepositoryGateway.findAcceptedFriendUserIds(userId));

        // 3. Gọi gRPC sang auth-service lấy danh sách ứng viên (loại trừ connectedIds)
        List<AuthServiceGateway.UserSocialInfo> candidates =
                authServiceGateway.getSuggestedUsers(userId, connectedIds, maxSuggestions * 3);

        if (candidates.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        java.util.Map<Long, String> friendNameCache = new java.util.HashMap<>();

        record ScoredCandidate(AuthServiceGateway.UserSocialInfo user, int mutualCount, List<String> mutualNames, long posts, int score) {}

        List<ScoredCandidate> scoredList = new java.util.ArrayList<>();

        for (AuthServiceGateway.UserSocialInfo candidate : candidates) {
            List<Long> candidateFriendIds = friendshipRepositoryGateway.findAcceptedFriendUserIds(candidate.id());
            List<Long> mutualIds = candidateFriendIds.stream().filter(myFriendIds::contains).toList();

            List<String> mutualNames = new java.util.ArrayList<>();
            for (Long mId : mutualIds) {
                if (mutualNames.size() >= 2) break;
                String mName = friendNameCache.computeIfAbsent(mId, id ->
                        authServiceGateway.getUserById(id).map(AuthServiceGateway.UserSocialInfo::name).orElse(""));
                if (!mName.isBlank()) {
                    mutualNames.add(mName);
                }
            }

            long postCount = postRepositoryGateway.countByUserId(candidate.id());
            int score = (mutualIds.size() * 10) + (postCount > 0 ? 5 : 0);

            scoredList.add(new ScoredCandidate(candidate, mutualIds.size(), mutualNames, postCount, score));
        }

        // Xếp điểm giảm dần
        scoredList.sort((a, b) -> {
            int cmp = Integer.compare(b.score(), a.score());
            if (cmp != 0) return cmp;
            return Long.compare(b.posts(), a.posts());
        });

        return scoredList.stream()
                .limit(maxSuggestions)
                .map(sc -> {
                    String reason;
                    if (sc.mutualCount() > 0) {
                        reason = "Có " + sc.mutualCount() + " bạn chung" +
                                (!sc.mutualNames().isEmpty() ? " (" + String.join(", ", sc.mutualNames()) + ")" : "");
                    } else if (sc.posts() > 0) {
                        reason = "Người sáng tạo có " + sc.posts() + " clip trên PENTAVA";
                    } else {
                        reason = "Gợi ý thành viên mới";
                    }

                    return com.nqt.social.usecase.dto.FriendSuggestionDTO.builder()
                            .userId(sc.user().id())
                            .name(sc.user().name())
                            .email(sc.user().email())
                            .avatarUrl(sc.user().avatarUrl())
                            .bio(sc.user().bio())
                            .mutualFriendsCount(sc.mutualCount())
                            .mutualFriendNames(sc.mutualNames())
                            .totalPosts(sc.posts())
                            .reason(reason)
                            .build();
                })
                .toList();
    }

    private FriendshipDTO toDto(Friendship f, Long friendUserId, AuthServiceGateway.UserSocialInfo u) {
        return FriendshipDTO.builder()
                .id(f.getId())
                .requesterId(f.getRequesterId())
                .addresseeId(f.getAddresseeId())
                .friendUserId(friendUserId)
                .friendName(u != null ? u.name() : "")
                .friendEmail(u != null ? u.email() : "")
                .friendAvatar(u != null ? u.avatarUrl() : "")
                .status(f.getStatus())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
