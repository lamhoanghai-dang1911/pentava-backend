package com.nqt.social.usecase.boundary;

import java.util.Optional;

public interface CinemaServiceGateway {

    record ClipSocialInfo(
            Long clipId,
            Long userId,
            Long goalId,
            Integer weekNumber,
            String status,
            String videoUrl,
            String storagePath,
            Integer photoCount
    ) {}

    Optional<ClipSocialInfo> getClipForSocial(Long clipId, Long requestingUserId);
}
