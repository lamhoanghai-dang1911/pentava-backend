package com.nqt.cinema.infrastructure.persistence;

import com.nqt.cinema.domain.Clip;
import com.nqt.cinema.domain.ClipPhotoRef;
import com.nqt.cinema.domain.ClipStatus;
import com.nqt.cinema.infrastructure.persistence.entity.ClipEntity;
import com.nqt.cinema.infrastructure.persistence.entity.ClipPhotoEntity;
import com.nqt.cinema.usecase.boundary.ClipRepositoryGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClipRepositoryJpaAdapter implements ClipRepositoryGateway {

    private static final List<ClipStatus> ACTIVE_STATUSES =
            List.of(ClipStatus.PENDING, ClipStatus.PROCESSING, ClipStatus.UPLOADING);

    private final ClipJpaRepository clipJpaRepository;

    @Override
    @Transactional
    public Clip create(Clip clip) {
        ClipEntity entity = ClipEntity.builder()
                .userId(clip.userId())
                .goalId(clip.goalId())
                .weekNumber(clip.weekNumber())
                .status(clip.status())
                .build();

        for (ClipPhotoRef photo : clip.photos()) {
            entity.getPhotos().add(ClipPhotoEntity.builder()
                    .clip(entity)
                    .photoId(photo.photoId())
                    .imageUrl(photo.imageUrl())
                    .checkinDate(photo.checkinDate())
                    .displayOrder(photo.displayOrder())
                    .build());
        }

        return toDomain(clipJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Clip> findByIdAndUserId(Long clipId, Long userId) {
        return clipJpaRepository.findByIdAndUserId(clipId, userId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Clip> findById(Long clipId) {
        return clipJpaRepository.findById(clipId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Clip> findByUserGoalWeek(Long userId, Long goalId, int weekNumber) {
        return clipJpaRepository
                .findByUserIdAndGoalIdAndWeekNumberOrderByCreatedAtDesc(userId, goalId, weekNumber)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public long countByUserGoalWeekAndStatus(Long userId, Long goalId, int weekNumber, ClipStatus status) {
        return clipJpaRepository.countByUserIdAndGoalIdAndWeekNumberAndStatus(userId, goalId, weekNumber, status);
    }

    @Override
    public boolean hasActiveJob(Long userId) {
        return clipJpaRepository.existsByUserIdAndStatusIn(userId, ACTIVE_STATUSES);
    }

    @Override
    @Transactional
    public void updateStatus(Long clipId, ClipStatus status, String storagePath, String errorMessage) {
        updateStatus(clipId, status, storagePath, null, errorMessage);
    }

    @Override
    @Transactional
    public void updateStatus(Long clipId, ClipStatus status, String storagePath, Long fileSizeBytes, String errorMessage) {
        ClipEntity entity = clipJpaRepository.findById(clipId)
                .orElseThrow(() -> new IllegalStateException("Clip " + clipId + " không tồn tại"));
        entity.setStatus(status);
        if (storagePath != null) {
            entity.setStoragePath(storagePath);
        }
        if (fileSizeBytes != null) {
            entity.setFileSizeBytes(fileSizeBytes);
        }
        entity.setErrorMessage(errorMessage);
        entity.setUpdatedAt(LocalDateTime.now());
        clipJpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public long sumFileSizeBytesByUserId(Long userId) {
        if (userId == null) return 0L;
        return clipJpaRepository.sumFileSizeBytesByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUserIdAndStatus(Long userId, ClipStatus status) {
        if (userId == null) return 0L;
        return clipJpaRepository.countByUserIdAndStatus(userId, status);
    }

    @Override
    @Transactional
    public void delete(Long clipId) {
        clipJpaRepository.deleteById(clipId);
    }

    private Clip toDomain(ClipEntity entity) {
        List<ClipPhotoRef> photos = (entity.getPhotos() == null) ? List.of() : entity.getPhotos().stream()
                .map(p -> new ClipPhotoRef(p.getPhotoId(), p.getImageUrl(), p.getCheckinDate(), p.getDisplayOrder()))
                .toList();

        return new Clip(
                entity.getId(), entity.getUserId(), entity.getGoalId(), entity.getWeekNumber(),
                entity.getStatus(), entity.getStoragePath(), entity.getFileSizeBytes(), entity.getErrorMessage(),
                entity.getCreatedAt(), entity.getUpdatedAt(), photos);
    }
}