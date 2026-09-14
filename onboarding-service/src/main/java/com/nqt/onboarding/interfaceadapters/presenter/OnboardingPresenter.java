package com.nqt.onboarding.interfaceadapters.presenter;

import com.nqt.onboarding.usecase.boundary.OnboardingOutputPort;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class OnboardingPresenter implements OnboardingOutputPort {
    private ResponseEntity<?> responseEntity;

    @Override
    public void present(Object data) {
        this.responseEntity = ResponseEntity.ok(data);
    }

    @Override
    public void presentError(String message) {
        this.responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", message));
    }

    @Override
    public ResponseEntity<?> getResponseEntity() {
        return responseEntity;
    }
}