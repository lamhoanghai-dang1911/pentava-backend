package com.nqt.onboarding.usecase.boundary;

import com.nqt.onboarding.usecase.dto.TaskProgressRequestDTO;
import com.nqt.onboarding.usecase.dto.TaskProgressResponseDTO;

public interface TaskInputPort {
    void saveTaskProgressItems(Long taskId, TaskProgressRequestDTO requestDTO);
    TaskProgressResponseDTO getTaskProgress(Long taskId);
}