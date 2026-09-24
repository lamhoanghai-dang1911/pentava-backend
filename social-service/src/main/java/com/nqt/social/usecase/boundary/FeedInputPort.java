package com.nqt.social.usecase.boundary;

import com.nqt.social.usecase.dto.FeedResponseDTO;

public interface FeedInputPort {
    FeedResponseDTO getForYouFeed(Long viewerId, String cursor, int limit);
    FeedResponseDTO getFriendsFeed(Long viewerId, String cursor, int limit);
}
