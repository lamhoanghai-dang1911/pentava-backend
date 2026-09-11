package com.nqt.onboarding.usecase.boundary;
//package com.nqt.onboarding.usecase.boundary;

import org.springframework.http.ResponseEntity;

public interface OnboardingOutputPort {
    void present(Object data);
    void presentError(String message);
    ResponseEntity<?> getResponseEntity();
}