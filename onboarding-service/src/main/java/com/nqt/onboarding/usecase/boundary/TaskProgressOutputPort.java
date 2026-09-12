package com.nqt.onboarding.usecase.boundary;

import com.nqt.onboarding.usecase.dto.TaskProgressResponseDTO;

public interface TaskProgressOutputPort {
    void present(TaskProgressResponseDTO response);
    void presentError(String message);
}