package com.nqt.social.usecase.boundary;

import com.nqt.social.usecase.dto.FriendshipDTO;

import java.util.List;

public interface FriendshipInputPort {
    FriendshipDTO sendFriendRequest(Long requesterId, Long targetUserId);
    FriendshipDTO acceptFriendRequest(Long friendshipId, Long userId);
    void rejectFriendRequest(Long friendshipId, Long userId);
    void unfriend(Long friendUserId, Long userId);
    List<FriendshipDTO> getFriends(Long userId);
    List<FriendshipDTO> getPendingRequests(Long userId);
    List<com.nqt.social.usecase.dto.FriendSuggestionDTO> getFriendSuggestions(Long userId, int limit);
}
