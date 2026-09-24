package com.nqt.social.usecase.boundary;

import java.util.Optional;

public interface AuthServiceGateway {

    record UserSocialInfo(
            Long id,
            String email,
            String name,
            String avatarUrl,
            String bio
    ) {}

    Optional<UserSocialInfo> getUserById(Long userId);

    Optional<Long> getUserIdByEmail(String email);

    java.util.List<UserSocialInfo> getSuggestedUsers(Long currentUserId, java.util.List<Long> excludedUserIds, int limit);
}
