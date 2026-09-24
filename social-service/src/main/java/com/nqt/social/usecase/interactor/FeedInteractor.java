package com.nqt.social.usecase.interactor;

import com.nqt.social.domain.Post;
import com.nqt.social.infrastructure.recommendation.FeedRecommendationEngine;
import com.nqt.social.usecase.boundary.*;
import com.nqt.social.usecase.dto.FeedResponseDTO;
import com.nqt.social.usecase.dto.PostResponseDTO;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeedInteractor implements FeedInputPort {

    private final PostRepositoryGateway postRepositoryGateway;
    private final HighFiveRepositoryGateway highFiveRepositoryGateway;
    private final FriendshipRepositoryGateway friendshipRepositoryGateway;
    private final AuthServiceGateway authServiceGateway;
    private final FeedRecommendationEngine recommendationEngine;

    public FeedInteractor(
            PostRepositoryGateway postRepositoryGateway,
            HighFiveRepositoryGateway highFiveRepositoryGateway,
            FriendshipRepositoryGateway friendshipRepositoryGateway,
            AuthServiceGateway authServiceGateway,
            FeedRecommendationEngine recommendationEngine) {
        this.postRepositoryGateway = postRepositoryGateway;
        this.highFiveRepositoryGateway = highFiveRepositoryGateway;
        this.friendshipRepositoryGateway = friendshipRepositoryGateway;
        this.authServiceGateway = authServiceGateway;
        this.recommendationEngine = recommendationEngine;
    }

    @Override
    public FeedResponseDTO getForYouFeed(Long viewerId, String cursor, int limit) {
        int pageSize = Math.max(1, Math.min(limit, 50));

        List<Long> friendIds = viewerId != null
                ? friendshipRepositoryGateway.findAcceptedFriendUserIds(viewerId)
                : Collections.emptyList();

        Set<Long> friendIdSet = new HashSet<>(friendIds);

        // Lấy tất cả bài viết hợp lệ (PUBLIC hoặc FRIENDS_ONLY của bạn bè)
        List<Post> candidates = postRepositoryGateway.findAllAccessible(viewerId, friendIds);
        if (candidates.isEmpty()) {
            return new FeedResponseDTO(Collections.emptyList(), null, false);
        }

        // Chấm điểm và xếp hạng bài viết bằng thuật toán đề xuất
        List<FeedRecommendationEngine.ScoredPost> scoredPosts = recommendationEngine.scoreAndRank(candidates, viewerId, friendIdSet);

        // Áp dụng Cursor Pagination
        FeedRecommendationEngine.CursorData cursorData = recommendationEngine.decodeCursor(cursor);

        int startIndex = 0;
        if (cursorData != null) {
            for (int i = 0; i < scoredPosts.size(); i++) {
                FeedRecommendationEngine.ScoredPost sp = scoredPosts.get(i);
                boolean isAfter = (sp.score() < cursorData.score()) ||
                        (Math.abs(sp.score() - cursorData.score()) < 0.00001 && sp.post().getId() < cursorData.postId());
                if (isAfter) {
                    startIndex = i;
                    break;
                }
                if (i == scoredPosts.size() - 1) {
                    // Đã tới cuối danh sách
                    startIndex = scoredPosts.size();
                }
            }
        }

        int endIndex = Math.min(startIndex + pageSize, scoredPosts.size());
        List<FeedRecommendationEngine.ScoredPost> paged = (startIndex < scoredPosts.size())
                ? scoredPosts.subList(startIndex, endIndex)
                : Collections.emptyList();

        boolean hasMore = endIndex < scoredPosts.size();
        String nextCursor = null;
        if (hasMore && !paged.isEmpty()) {
            FeedRecommendationEngine.ScoredPost last = paged.get(paged.size() - 1);
            nextCursor = recommendationEngine.encodeCursor(last.score(), last.post().getId());
        }

        List<Post> posts = paged.stream().map(FeedRecommendationEngine.ScoredPost::post).toList();
        List<PostResponseDTO> dtoList = mapToPostDtos(posts, viewerId);

        return new FeedResponseDTO(dtoList, nextCursor, hasMore);
    }

    @Override
    public FeedResponseDTO getFriendsFeed(Long viewerId, String cursor, int limit) {
        if (viewerId == null) {
            throw new IllegalStateException("Yêu cầu đăng nhập để xem bảng tin bạn bè");
        }

        int pageSize = Math.max(1, Math.min(limit, 50));
        List<Long> friendIds = friendshipRepositoryGateway.findAcceptedFriendUserIds(viewerId);

        if (friendIds.isEmpty()) {
            return new FeedResponseDTO(Collections.emptyList(), null, false);
        }

        List<Post> friendPosts = postRepositoryGateway.findFriendsPosts(friendIds);
        if (friendPosts.isEmpty()) {
            return new FeedResponseDTO(Collections.emptyList(), null, false);
        }

        // Bảng tin bạn bè sắp xếp theo thời gian mới nhất (chronological)
        friendPosts.sort((a, b) -> {
            int cmp = b.getCreatedAt().compareTo(a.getCreatedAt());
            return cmp != 0 ? cmp : Long.compare(b.getId(), a.getId());
        });

        // Xử lý cursor (timestampMillis:postId)
        long cursorTimestamp = Long.MAX_VALUE;
        long cursorPostId = Long.MAX_VALUE;
        if (cursor != null && !cursor.isBlank()) {
            try {
                String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
                String[] parts = raw.split(":");
                cursorTimestamp = Long.parseLong(parts[0]);
                cursorPostId = Long.parseLong(parts[1]);
            } catch (Exception ignored) {}
        }

        int startIndex = 0;
        if (cursorTimestamp != Long.MAX_VALUE) {
            for (int i = 0; i < friendPosts.size(); i++) {
                Post p = friendPosts.get(i);
                long pMillis = p.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli();
                if (pMillis < cursorTimestamp || (pMillis == cursorTimestamp && p.getId() < cursorPostId)) {
                    startIndex = i;
                    break;
                }
                if (i == friendPosts.size() - 1) {
                    startIndex = friendPosts.size();
                }
            }
        }

        int endIndex = Math.min(startIndex + pageSize, friendPosts.size());
        List<Post> paged = (startIndex < friendPosts.size())
                ? friendPosts.subList(startIndex, endIndex)
                : Collections.emptyList();

        boolean hasMore = endIndex < friendPosts.size();
        String nextCursor = null;
        if (hasMore && !paged.isEmpty()) {
            Post last = paged.get(paged.size() - 1);
            long millis = last.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli();
            String raw = millis + ":" + last.getId();
            nextCursor = Base64.getUrlEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        }

        List<PostResponseDTO> dtoList = mapToPostDtos(paged, viewerId);
        return new FeedResponseDTO(dtoList, nextCursor, hasMore);
    }

    private List<PostResponseDTO> mapToPostDtos(List<Post> posts, Long viewerId) {
        if (posts.isEmpty()) return Collections.emptyList();

        // Batch resolve author info
        Map<Long, AuthServiceGateway.UserSocialInfo> userCache = posts.stream()
                .map(Post::getUserId)
                .distinct()
                .map(id -> authServiceGateway.getUserById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(AuthServiceGateway.UserSocialInfo::id, u -> u, (a, b) -> a));

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Long> highFivedPostIds = (viewerId != null)
                ? highFiveRepositoryGateway.findPostIdsHighFivedByUser(viewerId, postIds)
                : Collections.emptyList();

        return posts.stream()
                .map(p -> {
                    AuthServiceGateway.UserSocialInfo author = userCache.get(p.getUserId());
                    return PostResponseDTO.builder()
                            .id(p.getId())
                            .userId(p.getUserId())
                            .authorName(author != null ? author.name() : "")
                            .authorAvatar(author != null ? author.avatarUrl() : "")
                            .clipId(p.getClipId())
                            .goalId(p.getGoalId())
                            .weekNumber(p.getWeekNumber())
                            .videoUrl(p.getVideoUrl())
                            .caption(p.getCaption())
                            .tags(p.getTags())
                            .privacyMode(p.getPrivacyMode())
                            .highFiveCount(p.getHighFiveCount())
                            .commentCount(p.getCommentCount())
                            .isHighFivedByMe(highFivedPostIds.contains(p.getId()))
                            .createdAt(p.getCreatedAt())
                            .updatedAt(p.getUpdatedAt())
                            .build();
                })
                .toList();
    }
}
