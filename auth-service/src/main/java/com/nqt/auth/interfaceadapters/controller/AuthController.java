package com.nqt.auth.interfaceadapters.controller;

import com.nqt.auth.interfaceadapters.presenter.AuthPresenter;
import com.nqt.auth.usecase.boundary.*;
import com.nqt.auth.usecase.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterInputPort registerInputPort;
    private final LoginInputPort loginInputPort;
    private final GoogleLoginInputPort googleLoginInputPort;
    private final VerifyOtpInputPort verifyOtpInputPort;
    private final AuthPresenter authPresenter;
    private final ResendOtpInputPort resendOtpInputPort;

    public AuthController(RegisterInputPort registerInputPort,
                          LoginInputPort loginInputPort,
                          GoogleLoginInputPort googleLoginInputPort,
                          VerifyOtpInputPort verifyOtpInputPort,
                          ResendOtpInputPort resendOtpInputPort,
                          AuthPresenter authPresenter) {
        this.registerInputPort = registerInputPort;
        this.loginInputPort = loginInputPort;
        this.googleLoginInputPort = googleLoginInputPort;
        this.verifyOtpInputPort = verifyOtpInputPort;
        this.resendOtpInputPort = resendOtpInputPort;
        this.authPresenter = authPresenter;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO requestDTO) {
        registerInputPort.register(requestDTO);
        return authPresenter.getResponseEntity();
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequestDTO requestDTO) {
        verifyOtpInputPort.verifyOtp(requestDTO);
        return authPresenter.getResponseEntity();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        loginInputPort.login(requestDTO);
        return authPresenter.getResponseEntity();
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleLoginRequestDTO requestDTO) {
        googleLoginInputPort.googleLogin(requestDTO);
        return authPresenter.getResponseEntity();
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody ResendOtpRequestDTO requestDTO) {
        resendOtpInputPort.resendOtp(requestDTO);
        return authPresenter.getResponseEntity();
    }
}