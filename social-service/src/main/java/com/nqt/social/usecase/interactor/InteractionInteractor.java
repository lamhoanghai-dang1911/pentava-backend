package com.nqt.social.usecase.interactor;

import com.nqt.social.domain.Comment;
import com.nqt.social.domain.HighFive;
import com.nqt.social.domain.Post;
import com.nqt.social.domain.SocialEvent;
import com.nqt.social.usecase.boundary.*;
import com.nqt.social.usecase.dto.AddCommentRequestDTO;
import com.nqt.social.usecase.dto.CommentResponseDTO;
import com.nqt.social.usecase.dto.HighFiveToggleResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InteractionInteractor implements InteractionInputPort {

    private final PostRepositoryGateway postRepositoryGateway;
    private final HighFiveRepositoryGateway highFiveRepositoryGateway;
    private final CommentRepositoryGateway commentRepositoryGateway;
    private final AuthServiceGateway authServiceGateway;
    private final SocialEventPublisherGateway socialEventPublisherGateway;

    public InteractionInteractor(
            PostRepositoryGateway postRepositoryGateway,
            HighFiveRepositoryGateway highFiveRepositoryGateway,
            CommentRepositoryGateway commentRepositoryGateway,
            AuthServiceGateway authServiceGateway,
            SocialEventPublisherGateway socialEventPublisherGateway) {
        this.postRepositoryGateway = postRepositoryGateway;
        this.highFiveRepositoryGateway = highFiveRepositoryGateway;
        this.commentRepositoryGateway = commentRepositoryGateway;
        this.authServiceGateway = authServiceGateway;
        this.socialEventPublisherGateway = socialEventPublisherGateway;
    }

    @Override
    @Transactional
    public HighFiveToggleResponseDTO toggleHighFive(Long postId, Long userId) {
        Post post = postRepositoryGateway.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết với ID: " + postId));

        boolean alreadyHighFived = highFiveRepositoryGateway.existsByPostIdAndUserId(postId, userId);
        boolean highFived;

        if (alreadyHighFived) {
            highFiveRepositoryGateway.deleteByPostIdAndUserId(postId, userId);
            post.setHighFiveCount(Math.max(0, post.getHighFiveCount() - 1));
            highFived = false;
        } else {
            HighFive hf = HighFive.builder()
                    .postId(postId)
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .build();
            highFiveRepositoryGateway.save(hf);
            post.setHighFiveCount(post.getHighFiveCount() + 1);
            highFived = true;

            // Bắn sự kiện sang Kafka nếu thả high-five cho bài của người khác
            if (!post.getUserId().equals(userId)) {
                AuthServiceGateway.UserSocialInfo actor = authServiceGateway.getUserById(userId).orElse(null);
                SocialEvent event = SocialEvent.builder()
                        .eventType(SocialEvent.EventType.HIGH_FIVE)
                        .actorId(userId)
                        .actorName(actor != null ? actor.name() : "Người dùng")
                        .actorAvatar(actor != null ? actor.avatarUrl() : "")
                        .targetUserId(post.getUserId())
                        .postId(postId)
                        .message((actor != null ? actor.name() : "Ai đó") + " đã thả High-Five bài viết của bạn!")
                        .timestamp(LocalDateTime.now())
                        .build();

                socialEventPublisherGateway.publish(event);
            }
        }

        postRepositoryGateway.updateCounters(postId, post.getHighFiveCount(), post.getCommentCount());

        return HighFiveToggleResponseDTO.builder()
                .postId(postId)
                .highFived(highFived)
                .totalHighFives(post.getHighFiveCount())
                .build();
    }

    @Override
    @Transactional
    public CommentResponseDTO addComment(Long postId, Long userId, AddCommentRequestDTO request) {
        Post post = postRepositoryGateway.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết với ID: " + postId));

        Comment comment = Comment.builder()
                .postId(postId)
                .userId(userId)
                .parentId(request.getParentId())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepositoryGateway.save(comment);

        // Tăng đếm comment
        post.setCommentCount(post.getCommentCount() + 1);
        postRepositoryGateway.updateCounters(postId, post.getHighFiveCount(), post.getCommentCount());

        AuthServiceGateway.UserSocialInfo actor = authServiceGateway.getUserById(userId).orElse(null);

        // Bắn sự kiện sang Kafka cho tác giả bài viết
        if (!post.getUserId().equals(userId)) {
            SocialEvent event = SocialEvent.builder()
                    .eventType(SocialEvent.EventType.COMMENT)
                    .actorId(userId)
                    .actorName(actor != null ? actor.name() : "Người dùng")
                    .actorAvatar(actor != null ? actor.avatarUrl() : "")
                    .targetUserId(post.getUserId())
                    .postId(postId)
                    .commentId(savedComment.getId())
                    .message((actor != null ? actor.name() : "Ai đó") + " đã bình luận: \"" + request.getContent() + "\"")
                    .timestamp(LocalDateTime.now())
                    .build();

            socialEventPublisherGateway.publish(event);
        }

        return CommentResponseDTO.builder()
                .id(savedComment.getId())
                .postId(postId)
                .userId(userId)
                .authorName(actor != null ? actor.name() : "")
                .authorAvatar(actor != null ? actor.avatarUrl() : "")
                .parentId(savedComment.getParentId())
                .content(savedComment.getContent())
                .createdAt(savedComment.getCreatedAt())
                .build();
    }

    @Override
    public List<CommentResponseDTO> getComments(Long postId, Long viewerId) {
        postRepositoryGateway.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết với ID: " + postId));

        List<Comment> comments = commentRepositoryGateway.findByPostIdOrderByCreatedAtAsc(postId);
        if (comments.isEmpty()) {
            return List.of();
        }

        // Lấy thông tin user cho mỗi comment
        Map<Long, AuthServiceGateway.UserSocialInfo> userCache = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .map(id -> authServiceGateway.getUserById(id).orElse(null))
                .filter(u -> u != null)
                .collect(Collectors.toMap(AuthServiceGateway.UserSocialInfo::id, u -> u, (a, b) -> a));

        return comments.stream()
                .map(c -> {
                    AuthServiceGateway.UserSocialInfo u = userCache.get(c.getUserId());
                    return CommentResponseDTO.builder()
                            .id(c.getId())
                            .postId(c.getPostId())
                            .userId(c.getUserId())
                            .authorName(u != null ? u.name() : "")
                            .authorAvatar(u != null ? u.avatarUrl() : "")
                            .parentId(c.getParentId())
                            .content(c.getContent())
                            .createdAt(c.getCreatedAt())
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepositoryGateway.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bình luận với ID: " + commentId));

        Post post = postRepositoryGateway.findById(comment.getPostId()).orElse(null);

        boolean isCommentAuthor = comment.getUserId().equals(userId);
        boolean isPostAuthor = post != null && post.getUserId().equals(userId);

        if (!isCommentAuthor && !isPostAuthor) {
            throw new IllegalStateException("Bạn không có quyền xoá bình luận này");
        }

        commentRepositoryGateway.deleteById(commentId);

        if (post != null) {
            post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
            postRepositoryGateway.updateCounters(post.getId(), post.getHighFiveCount(), post.getCommentCount());
        }
    }
}
