package com.nqt.onboarding.interfaceadapters.controller;

import com.nqt.onboarding.interfaceadapters.presenter.OnboardingPresenter;
import com.nqt.onboarding.usecase.boundary.OnboardingInputPort;
import com.nqt.onboarding.usecase.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingInputPort inputPort;
    private final OnboardingPresenter presenter;

    public OnboardingController(OnboardingInputPort inputPort, OnboardingPresenter presenter) {
        this.inputPort = inputPort;
        this.presenter = presenter;
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitOnboarding(@Valid @RequestBody OnboardingRequestDTO requestDTO) {
        inputPort.submitOnboarding(requestDTO);
        return presenter.getResponseEntity();
    }

    @PostMapping("/next-goal")
    public ResponseEntity<?> progressGoal(@Valid @RequestBody GoalProgressionRequestDTO requestDTO) {
        inputPort.progressToNextGoal(requestDTO);
        return presenter.getResponseEntity();
    }
}