package com.nqt.social.usecase.interactor;

import com.nqt.social.domain.Post;
import com.nqt.social.domain.PrivacyMode;
import com.nqt.social.usecase.boundary.*;
import com.nqt.social.usecase.dto.CreatePostRequestDTO;
import com.nqt.social.usecase.dto.PostResponseDTO;
import com.nqt.social.usecase.dto.UpdatePostRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class PostInteractor implements PostInputPort {

    private final PostRepositoryGateway postRepositoryGateway;
    private final HighFiveRepositoryGateway highFiveRepositoryGateway;
    private final FriendshipRepositoryGateway friendshipRepositoryGateway;
    private final CinemaServiceGateway cinemaServiceGateway;
    private final AuthServiceGateway authServiceGateway;

    public PostInteractor(
            PostRepositoryGateway postRepositoryGateway,
            HighFiveRepositoryGateway highFiveRepositoryGateway,
            FriendshipRepositoryGateway friendshipRepositoryGateway,
            CinemaServiceGateway cinemaServiceGateway,
            AuthServiceGateway authServiceGateway) {
        this.postRepositoryGateway = postRepositoryGateway;
        this.highFiveRepositoryGateway = highFiveRepositoryGateway;
        this.friendshipRepositoryGateway = friendshipRepositoryGateway;
        this.cinemaServiceGateway = cinemaServiceGateway;
        this.authServiceGateway = authServiceGateway;
    }

    @Override
    @Transactional
    public PostResponseDTO createPost(Long userId, CreatePostRequestDTO request) {
        // 1. Gọi cinema-service qua gRPC để xác thực clip
        CinemaServiceGateway.ClipSocialInfo clipInfo = cinemaServiceGateway.getClipForSocial(request.getClipId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Không thể tìm thấy hoặc xác thực clip với cinema-service"));

        if (!clipInfo.userId().equals(userId)) {
            throw new IllegalArgumentException("Bạn chỉ có thể đăng clip của chính mình");
        }

        if (!"COMPLETED".equalsIgnoreCase(clipInfo.status())) {
            throw new IllegalStateException("Clip chưa hoàn tất quá trình render, không thể đăng bài");
        }

        // 2. Tạo Post
        Post post = Post.builder()
                .userId(userId)
                .clipId(clipInfo.clipId())
                .goalId(clipInfo.goalId())
                .weekNumber(clipInfo.weekNumber())
                .videoUrl(clipInfo.videoUrl())
                .caption(request.getCaption())
                .tags(request.getTags())
                .privacyMode(request.getPrivacyMode() != null ? request.getPrivacyMode() : PrivacyMode.PUBLIC)
                .highFiveCount(0)
                .commentCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Post savedPost = postRepositoryGateway.save(post);

        // 3. Lấy thông tin tác giả qua gRPC auth-service
        AuthServiceGateway.UserSocialInfo authorInfo = authServiceGateway.getUserById(userId).orElse(null);

        return toDto(savedPost, authorInfo, false);
    }

    @Override
    @Transactional
    public PostResponseDTO updatePost(Long postId, Long userId, UpdatePostRequestDTO request) {
        Post post = postRepositoryGateway.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết với ID: " + postId));

        if (!post.getUserId().equals(userId)) {
            throw new IllegalStateException("Bạn không có quyền chỉnh sửa bài viết này");
        }

        if (request.getCaption() != null) {
            post.setCaption(request.getCaption());
        }
        if (request.getTags() != null) {
            post.setTags(request.getTags());
        }
        if (request.getPrivacyMode() != null) {
            post.setPrivacyMode(request.getPrivacyMode());
        }
        post.setUpdatedAt(LocalDateTime.now());

        Post updatedPost = postRepositoryGateway.save(post);

        AuthServiceGateway.UserSocialInfo authorInfo = authServiceGateway.getUserById(userId).orElse(null);
        boolean highFived = highFiveRepositoryGateway.existsByPostIdAndUserId(postId, userId);

        return toDto(updatedPost, authorInfo, highFived);
    }

    @Override
    public PostResponseDTO getPost(Long postId, Long viewerId) {
        Post post = postRepositoryGateway.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết với ID: " + postId));

        // Kiểm tra quyền riêng tư
        checkViewPermission(post, viewerId);

        AuthServiceGateway.UserSocialInfo authorInfo = authServiceGateway.getUserById(post.getUserId()).orElse(null);
        boolean highFived = viewerId != null && highFiveRepositoryGateway.existsByPostIdAndUserId(postId, viewerId);

        return toDto(post, authorInfo, highFived);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepositoryGateway.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết với ID: " + postId));

        if (!post.getUserId().equals(userId)) {
            throw new IllegalStateException("Bạn không có quyền xoá bài viết này");
        }

        postRepositoryGateway.deleteById(postId);
    }

    @Override
    public List<PostResponseDTO> getUserPosts(Long targetUserId, Long viewerId) {
        boolean isOwner = viewerId != null && viewerId.equals(targetUserId);
        boolean isFriend = false;

        if (!isOwner && viewerId != null) {
            isFriend = friendshipRepositoryGateway.findAcceptedFriendUserIds(viewerId).contains(targetUserId);
        }

        List<PrivacyMode> allowedModes;
        if (isOwner) {
            allowedModes = List.of(PrivacyMode.PUBLIC, PrivacyMode.FRIENDS_ONLY, PrivacyMode.PRIVATE);
        } else if (isFriend) {
            allowedModes = List.of(PrivacyMode.PUBLIC, PrivacyMode.FRIENDS_ONLY);
        } else {
            allowedModes = List.of(PrivacyMode.PUBLIC);
        }

        List<Post> posts = postRepositoryGateway.findByUserIdAndVisible(targetUserId, allowedModes);
        if (posts.isEmpty()) {
            return Collections.emptyList();
        }

        AuthServiceGateway.UserSocialInfo authorInfo = authServiceGateway.getUserById(targetUserId).orElse(null);

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Long> highFivedPostIds = (viewerId != null)
                ? highFiveRepositoryGateway.findPostIdsHighFivedByUser(viewerId, postIds)
                : Collections.emptyList();

        return posts.stream()
                .map(p -> toDto(p, authorInfo, highFivedPostIds.contains(p.getId())))
                .toList();
    }

    private void checkViewPermission(Post post, Long viewerId) {
        if (post.getPrivacyMode() == PrivacyMode.PUBLIC) {
            return;
        }

        if (viewerId == null) {
            throw new IllegalStateException("Bài viết yêu cầu đăng nhập để xem");
        }

        if (viewerId.equals(post.getUserId())) {
            return;
        }

        if (post.getPrivacyMode() == PrivacyMode.PRIVATE) {
            throw new IllegalStateException("Bài viết này ở chế độ riêng tư");
        }

        if (post.getPrivacyMode() == PrivacyMode.FRIENDS_ONLY) {
            boolean isFriend = friendshipRepositoryGateway.findAcceptedFriendUserIds(viewerId).contains(post.getUserId());
            if (!isFriend) {
                throw new IllegalStateException("Bài viết chỉ dành cho bạn bè của người đăng");
            }
        }
    }

    private PostResponseDTO toDto(Post post, AuthServiceGateway.UserSocialInfo authorInfo, boolean highFivedByMe) {
        return PostResponseDTO.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .authorName(authorInfo != null ? authorInfo.name() : "")
                .authorAvatar(authorInfo != null ? authorInfo.avatarUrl() : "")
                .clipId(post.getClipId())
                .goalId(post.getGoalId())
                .weekNumber(post.getWeekNumber())
                .videoUrl(post.getVideoUrl())
                .caption(post.getCaption())
                .tags(post.getTags())
                .privacyMode(post.getPrivacyMode())
                .highFiveCount(post.getHighFiveCount())
                .commentCount(post.getCommentCount())
                .isHighFivedByMe(highFivedByMe)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
