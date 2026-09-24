package com.nqt.social.usecase.boundary;

import com.nqt.social.usecase.dto.UserProfileResponseDTO;

public interface ProfileInputPort {
    UserProfileResponseDTO getProfile(Long targetUserId, Long viewerId);
}
