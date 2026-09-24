package com.nqt.social.usecase.interactor;

import com.nqt.social.domain.Friendship;
import com.nqt.social.domain.FriendshipStatus;
import com.nqt.social.usecase.boundary.*;
import com.nqt.social.usecase.dto.UserProfileResponseDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileInteractor implements ProfileInputPort {

    private final AuthServiceGateway authServiceGateway;
    private final PostRepositoryGateway postRepositoryGateway;
    private final HighFiveRepositoryGateway highFiveRepositoryGateway;
    private final FriendshipRepositoryGateway friendshipRepositoryGateway;

    public ProfileInteractor(
            AuthServiceGateway authServiceGateway,
            PostRepositoryGateway postRepositoryGateway,
            HighFiveRepositoryGateway highFiveRepositoryGateway,
            FriendshipRepositoryGateway friendshipRepositoryGateway) {
        this.authServiceGateway = authServiceGateway;
        this.postRepositoryGateway = postRepositoryGateway;
        this.highFiveRepositoryGateway = highFiveRepositoryGateway;
        this.friendshipRepositoryGateway = friendshipRepositoryGateway;
    }

    @Override
    public UserProfileResponseDTO getProfile(Long targetUserId, Long viewerId) {
        // 1. Gọi gRPC sang auth-service lấy thông tin cơ bản
        AuthServiceGateway.UserSocialInfo userInfo = authServiceGateway.getUserById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + targetUserId));

        // 2. Tổng hợp số liệu social
        long totalPosts = postRepositoryGateway.countByUserId(targetUserId);
        long totalHighFives = highFiveRepositoryGateway.countTotalHighFivesReceivedByUser(targetUserId);
        long totalFriends = friendshipRepositoryGateway.countAcceptedFriends(targetUserId);

        // 3. Xác định quan hệ với người xem
        boolean isMe = viewerId != null && viewerId.equals(targetUserId);
        String friendshipStatus = "NONE";

        if (isMe) {
            friendshipStatus = "ME";
        } else if (viewerId != null) {
            Optional<Friendship> friendshipOpt = friendshipRepositoryGateway.findBetween(viewerId, targetUserId);
            if (friendshipOpt.isPresent()) {
                Friendship f = friendshipOpt.get();
                if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                    friendshipStatus = "FRIENDS";
                } else if (f.getStatus() == FriendshipStatus.PENDING) {
                    friendshipStatus = f.getRequesterId().equals(viewerId) ? "PENDING_SENT" : "PENDING_RECEIVED";
                }
            }
        }

        return UserProfileResponseDTO.builder()
                .userId(targetUserId)
                .name(userInfo.name())
                .email(userInfo.email())
                .avatarUrl(userInfo.avatarUrl())
                .bio(userInfo.bio())
                .totalPosts(totalPosts)
                .totalHighFivesReceived(totalHighFives)
                .totalFriends(totalFriends)
                .friendshipStatusWithMe(friendshipStatus)
                .isMe(isMe)
                .build();
    }
}
