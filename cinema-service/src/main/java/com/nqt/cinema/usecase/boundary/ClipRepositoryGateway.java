package com.nqt.cinema.usecase.boundary;

import com.nqt.cinema.domain.Clip;
import com.nqt.cinema.domain.ClipStatus;

import java.util.List;
import java.util.Optional;

public interface ClipRepositoryGateway {
    Clip create(Clip clip);
    Optional<Clip> findByIdAndUserId(Long clipId, Long userId);
    Optional<Clip> findById(Long clipId); // dùng cho worker chạy nền, không có userId trong context
    List<Clip> findByUserGoalWeek(Long userId, Long goalId, int weekNumber);
    long countByUserGoalWeekAndStatus(Long userId, Long goalId, int weekNumber, ClipStatus status);
    boolean hasActiveJob(Long userId);
    void updateStatus(Long clipId, ClipStatus status, String storagePath, String errorMessage);
    void updateStatus(Long clipId, ClipStatus status, String storagePath, Long fileSizeBytes, String errorMessage);
    long sumFileSizeBytesByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, ClipStatus status);
    void delete(Long clipId);
}