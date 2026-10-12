package com.nqt.cinema.usecase.boundary;

import com.nqt.cinema.usecase.dto.ClipDTO;
import com.nqt.cinema.usecase.dto.CreateClipRequestDTO;

import java.util.List;

public interface ClipInputPort {
    ClipDTO createClip(Long goalId, int weekNumber, CreateClipRequestDTO request);
    ClipDTO createJourneyClip(Long goalId, com.nqt.cinema.usecase.dto.CreateJourneyClipRequestDTO request);
    List<ClipDTO> getClips(Long goalId, int weekNumber);
    ClipDTO getClip(Long clipId);
    void deleteClip(Long clipId);
}