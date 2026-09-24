package com.nqt.social.infrastructure.persistence.adapter;

import com.nqt.social.domain.Post;
import com.nqt.social.domain.PrivacyMode;
import com.nqt.social.infrastructure.persistence.entity.PostEntity;
import com.nqt.social.infrastructure.persistence.repository.PostJpaRepository;
import com.nqt.social.usecase.boundary.PostRepositoryGateway;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class PostRepositoryJpaAdapter implements PostRepositoryGateway {

    private final PostJpaRepository jpaRepository;

    public PostRepositoryJpaAdapter(PostJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Post save(Post post) {
        PostEntity entity = toEntity(post);
        PostEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Post> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Post> findByUserId(Long userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Post> findByUserIdAndVisible(Long userId, List<PrivacyMode> allowedModes) {
        return jpaRepository.findByUserIdAndPrivacyModeInOrderByCreatedAtDesc(userId, allowedModes).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByUserId(Long userId) {
        return jpaRepository.countByUserId(userId);
    }

    @Override
    public List<Post> findAllAccessible(Long viewerId, List<Long> friendIds) {
        List<Long> friends = (friendIds == null || friendIds.isEmpty()) ? List.of(-1L) : friendIds;
        return jpaRepository.findAllAccessiblePosts(viewerId, friends).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Post> findFriendsPosts(List<Long> friendIds) {
        if (friendIds == null || friendIds.isEmpty()) {
            return Collections.emptyList();
        }
        return jpaRepository.findFriendsPosts(friendIds).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void updateCounters(Long postId, long highFiveCount, long commentCount) {
        jpaRepository.updateCounters(postId, highFiveCount, commentCount);
    }

    private Post toDomain(PostEntity e) {
        return Post.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .clipId(e.getClipId())
                .goalId(e.getGoalId())
                .weekNumber(e.getWeekNumber())
                .videoUrl(e.getVideoUrl())
                .caption(e.getCaption())
                .tags(e.getTags())
                .privacyMode(e.getPrivacyMode())
                .highFiveCount(e.getHighFiveCount())
                .commentCount(e.getCommentCount())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private PostEntity toEntity(Post d) {
        return PostEntity.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .clipId(d.getClipId())
                .goalId(d.getGoalId())
                .weekNumber(d.getWeekNumber())
                .videoUrl(d.getVideoUrl())
                .caption(d.getCaption())
                .tags(d.getTags())
                .privacyMode(d.getPrivacyMode())
                .highFiveCount(d.getHighFiveCount())
                .commentCount(d.getCommentCount())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
