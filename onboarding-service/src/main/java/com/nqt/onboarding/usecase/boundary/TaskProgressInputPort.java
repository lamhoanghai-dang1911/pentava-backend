package com.nqt.onboarding.usecase.boundary;

public interface TaskProgressInputPort {
    void getTaskProgress(Long taskId);
    void incrementTaskProgress(Long taskId);
}