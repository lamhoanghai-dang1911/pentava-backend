package com.nqt.social.usecase.boundary;

import com.nqt.social.domain.Post;
import com.nqt.social.domain.PrivacyMode;

import java.util.List;
import java.util.Optional;

public interface PostRepositoryGateway {
    Post save(Post post);
    Optional<Post> findById(Long id);
    void deleteById(Long id);
    List<Post> findByUserId(Long userId);
    List<Post> findByUserIdAndVisible(Long userId, List<PrivacyMode> allowedModes);
    long countByUserId(Long userId);
    List<Post> findAllAccessible(Long viewerId, List<Long> friendIds);
    List<Post> findFriendsPosts(List<Long> friendIds);
    void updateCounters(Long postId, long highFiveCount, long commentCount);
}
