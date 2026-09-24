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
    private final ForgotPasswordInputPort forgotPasswordInputPort;
    private final VerifyResetOtpInputPort verifyResetOtpInputPort;
    private final ResetPasswordInputPort resetPasswordInputPort;
    private final UpdateProfileInputPort updateProfileInputPort;
    private final GetUserQueryInputPort getUserQueryInputPort;
    private final com.nqt.auth.infrastructure.security.JwtTokenProvider jwtTokenProvider;

    public AuthController(RegisterInputPort registerInputPort,
                          LoginInputPort loginInputPort,
                          GoogleLoginInputPort googleLoginInputPort,
                          VerifyOtpInputPort verifyOtpInputPort,
                          ResendOtpInputPort resendOtpInputPort,
                          ForgotPasswordInputPort forgotPasswordInputPort,
                          VerifyResetOtpInputPort verifyResetOtpInputPort,
                          ResetPasswordInputPort resetPasswordInputPort,
                          UpdateProfileInputPort updateProfileInputPort,
                          GetUserQueryInputPort getUserQueryInputPort,
                          com.nqt.auth.infrastructure.security.JwtTokenProvider jwtTokenProvider,
                          AuthPresenter authPresenter) {
        this.registerInputPort = registerInputPort;
        this.loginInputPort = loginInputPort;
        this.googleLoginInputPort = googleLoginInputPort;
        this.verifyOtpInputPort = verifyOtpInputPort;
        this.resendOtpInputPort = resendOtpInputPort;
        this.forgotPasswordInputPort = forgotPasswordInputPort;
        this.verifyResetOtpInputPort = verifyResetOtpInputPort;
        this.resetPasswordInputPort = resetPasswordInputPort;
        this.updateProfileInputPort = updateProfileInputPort;
        this.getUserQueryInputPort = getUserQueryInputPort;
        this.jwtTokenProvider = jwtTokenProvider;
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

    // ➕ Quên mật khẩu
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO requestDTO) {
        forgotPasswordInputPort.forgotPassword(requestDTO);
        return authPresenter.getResponseEntity();
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<?> verifyResetOtp(@Valid @RequestBody VerifyResetOtpRequestDTO requestDTO) {
        verifyResetOtpInputPort.verifyResetOtp(requestDTO);
        return authPresenter.getResponseEntity();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO requestDTO) {
        resetPasswordInputPort.resetPassword(requestDTO);
        return authPresenter.getResponseEntity();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body("Token không hợp lệ hoặc đã hết hạn");
        }
        String email = jwtTokenProvider.getEmailFromToken(token);
        return getUserQueryInputPort.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                           @Valid @RequestBody UpdateProfileRequestDTO requestDTO) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body("Token không hợp lệ hoặc đã hết hạn");
        }
        String email = jwtTokenProvider.getEmailFromToken(token);
        UserQueryResponseDTO updated = updateProfileInputPort.updateProfile(email, requestDTO);
        return ResponseEntity.ok(updated);
    }
}