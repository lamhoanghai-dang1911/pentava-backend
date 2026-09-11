package com.nqt.auth.usecase.interactor;

import com.nqt.auth.domain.User;
import com.nqt.auth.infrastructure.security.JwtTokenProvider;
import com.nqt.auth.infrastructure.service.EmailService;
import com.nqt.auth.usecase.boundary.*;
import com.nqt.auth.usecase.dto.*;
import com.nqt.auth.usecase.exception.BusinessException;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Transactional
public class AuthInteractor implements RegisterInputPort, LoginInputPort, GoogleLoginInputPort, VerifyOtpInputPort, ResendOtpInputPort {

    private final UserRepositoryGateway userRepository;
    private final LoginOutputPort outputPort;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthInteractor(UserRepositoryGateway userRepository,
                          LoginOutputPort outputPort,
                          EmailService emailService,
                          JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.outputPort = outputPort;
        this.emailService = emailService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void register(RegisterRequestDTO requestDTO) {
        Optional<User> existingUser = userRepository.findByEmail(requestDTO.getEmail());

        if (existingUser.isPresent()) {
            // Thay vì presentError, ta throw BusinessException để GlobalExceptionHandler bắt trọn
            throw new BusinessException("Email này đã được sử dụng. Vui lòng chọn email khác.");
        }

        // Sinh mã OTP ngẫu nhiên 6 chữ số
        String otpCode = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5); // Hết hạn sau 5 phút

        User newUser = User.builder()
                .email(requestDTO.getEmail())
                .password(requestDTO.getPassword()) // Lưu ý thực tế nên mã hóa BCrypt
                .name(requestDTO.getName())
                .authProvider("LOCAL")
                .otpCode(otpCode)
                .otpExpiryTime(expiryTime)
                .isVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(newUser);

        // Gửi email OTP
        emailService.sendOtpEmail(newUser.getEmail(), otpCode);

        LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                .email(newUser.getEmail())
                .message("Đăng ký thành công. Vui lòng kiểm tra email để nhận mã OTP xác thực.")
                .build();

        outputPort.present(responseDTO);
    }

    @Override
    public void verifyOtp(VerifyOtpRequestDTO requestDTO) {
        Optional<User> userOpt = userRepository.findByEmail(requestDTO.getEmail());

        if (userOpt.isEmpty()) {
            outputPort.presentError("Không tìm thấy tài khoản tương ứng với email");
            return;
        }

        User user = userOpt.get();

        if (user.isVerified()) {
            outputPort.presentError("Tài khoản này đã được xác thực từ trước");
            return;
        }

        if (user.getOtpCode() == null || !user.getOtpCode().equals(requestDTO.getOtpCode())) {
            outputPort.presentError("Mã OTP không chính xác");
            return;
        }

        if (user.getOtpExpiryTime() != null && LocalDateTime.now().isAfter(user.getOtpExpiryTime())) {
            outputPort.presentError("Mã OTP đã hết hạn");
            return;
        }

        // Xác thực thành công -> Kích hoạt tài khoản
        user.setVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiryTime(null);
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getEmail());

        LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                .accessToken(token)
                .email(user.getEmail())
                .message("Đăng nhập thành công")
                .build();

        outputPort.present(responseDTO);
    }

    @Override
    public void login(LoginRequestDTO requestDTO) {
        Optional<User> userOpt = userRepository.findByEmail(requestDTO.getEmail());

        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(requestDTO.getPassword())) {
            outputPort.presentError("Email hoặc mật khẩu không chính xác");
            return;
        }

        User user = userOpt.get();
        if (!user.isVerified()) {
            outputPort.presentError("Tài khoản chưa được xác thực OTP. Vui lòng kiểm tra email.");
            return;
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());

        LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                .accessToken(token)
                .email(user.getEmail())
                .message("Đăng nhập thành công")
                .build();

        outputPort.present(responseDTO);
    }

    @Override
    public void googleLogin(GoogleLoginRequestDTO requestDTO) {
        String googleEmail = "google.user@gmail.com";
        String googleName = "Google User";

        Optional<User> userOpt = userRepository.findByEmail(googleEmail);
        User user;

        if (userOpt.isEmpty()) {
            User newUser = User.builder()
                    .email(googleEmail)
                    .password("")
                    .name(googleName)
                    .authProvider("GOOGLE")
                    .isVerified(true) // Google login tự động active luôn
                    .createdAt(LocalDateTime.now())
                    .build();
            user = userRepository.save(newUser);
        } else {
            user = userOpt.get();
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());

        LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                .accessToken(token)
                .email(user.getEmail())
                .message("Đăng nhập thành công")
                .build();

        outputPort.present(responseDTO);
    }

    @Override
    public void resendOtp(ResendOtpRequestDTO requestDTO) {
        Optional<User> userOpt = userRepository.findByEmail(requestDTO.getEmail());

        if (userOpt.isEmpty()) {
            outputPort.presentError("Không tìm thấy tài khoản tương ứng với email");
            return;
        }

        User user = userOpt.get();

        if (user.isVerified()) {
            outputPort.presentError("Tài khoản này đã được xác thực trước đó");
            return;
        }

        // Sinh mã OTP mới hoàn toàn
        String newOtpCode = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime newExpiryTime = LocalDateTime.now().plusMinutes(5); // Gia hạn thêm 5 phút

        // Cập nhật thông tin OTP vào database
        user.setOtpCode(newOtpCode);
        user.setOtpExpiryTime(newExpiryTime);
        userRepository.save(user);

        // Gửi lại email OTP mới
        emailService.sendOtpEmail(user.getEmail(), newOtpCode);

        LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                .email(user.getEmail())
                .message("Mã OTP mới đã được gửi thành công đến email của bạn.")
                .build();

        outputPort.present(responseDTO);
    }
}