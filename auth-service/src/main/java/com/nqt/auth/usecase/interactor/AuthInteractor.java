package com.nqt.auth.usecase.interactor;

import com.nqt.auth.domain.User;
import com.nqt.auth.infrastructure.security.JwtTokenProvider;
import com.nqt.auth.infrastructure.service.EmailService;
import com.nqt.auth.usecase.boundary.*;
import com.nqt.auth.usecase.dto.*;
import com.nqt.auth.usecase.exception.BusinessException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@Transactional
public class AuthInteractor implements RegisterInputPort, LoginInputPort, GoogleLoginInputPort,
        VerifyOtpInputPort, ResendOtpInputPort, ForgotPasswordInputPort, VerifyResetOtpInputPort, ResetPasswordInputPort {

    private static final int MAX_RESET_OTP_ATTEMPTS = 5;
    private static final int RESET_OTP_LOCK_MINUTES = 15;
    private static final int RESET_OTP_EXPIRY_MINUTES = 5;
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 10;

    private final UserRepositoryGateway userRepository;
    private final LoginOutputPort outputPort;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final GoogleTokenGateway googleTokenGateway; // ➕

    public AuthInteractor(UserRepositoryGateway userRepository,
                          LoginOutputPort outputPort,
                          EmailService emailService,
                          JwtTokenProvider jwtTokenProvider,
                          PasswordEncoder passwordEncoder,
                          GoogleTokenGateway googleTokenGateway) {
        this.userRepository = userRepository;
        this.outputPort = outputPort;
        this.emailService = emailService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.googleTokenGateway = googleTokenGateway;
    }

    @Override
    public void register(RegisterRequestDTO requestDTO) {
        if (!requestDTO.getPassword().equals(requestDTO.getConfirmPassword())) {
            throw new BusinessException("Mật khẩu nhập lại không khớp");
        }

        Optional<User> existingUser = userRepository.findByEmail(requestDTO.getEmail());

        if (existingUser.isPresent()) {
            throw new BusinessException("Email này đã được sử dụng. Vui lòng chọn email khác.");
        }

        String otpCode = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);

        User newUser = User.builder()
                .email(requestDTO.getEmail())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .authProvider("LOCAL")
                .otpCode(otpCode)
                .otpExpiryTime(expiryTime)
                .isVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(newUser);
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

        user.setVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiryTime(null);
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getEmail());

        LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                .accessToken(token)
                .email(user.getEmail())
                .message("Đăng nhập thành công")
                .hasCompletedOnboarding(user.isHasCompletedOnboarding())
                .build();

        outputPort.present(responseDTO);
    }

    @Override
    public void login(LoginRequestDTO requestDTO) {
        Optional<User> userOpt = userRepository.findByEmail(requestDTO.getEmail());

        if (userOpt.isEmpty() || !passwordEncoder.matches(requestDTO.getPassword(), userOpt.get().getPassword())) {
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
                .hasCompletedOnboarding(user.isHasCompletedOnboarding())
                .build();

        outputPort.present(responseDTO);
    }

    @Override
    public void googleLogin(GoogleLoginRequestDTO requestDTO) {
        // ➕ Verify idToken thật với Google, lấy email/name/sub đã xác thực
        GoogleUserInfo googleUserInfo = googleTokenGateway.verify(requestDTO.getIdToken());

        Optional<User> userOpt = userRepository.findByEmail(googleUserInfo.email());
        User user;

        if (userOpt.isPresent()) {
            user = userOpt.get();

            if (!"GOOGLE".equals(user.getAuthProvider())) {
                outputPort.presentError("Email này đã được đăng ký bằng phương thức khác. Vui lòng đăng nhập bằng email và mật khẩu.");
                return;
            }

            // Backfill googleId cho account Google cũ (nếu tạo trước khi có field này)
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleUserInfo.googleId());
                userRepository.save(user);
            }
        } else {
            User newUser = User.builder()
                    .email(googleUserInfo.email())
                    .password("")
                    .name(googleUserInfo.name())
                    .authProvider("GOOGLE")
                    .googleId(googleUserInfo.googleId())
                    .isVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            user = userRepository.save(newUser);
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());

        LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                .accessToken(token)
                .email(user.getEmail())
                .message("Đăng nhập thành công")
                .hasCompletedOnboarding(user.isHasCompletedOnboarding())
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

        String newOtpCode = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime newExpiryTime = LocalDateTime.now().plusMinutes(5);

        user.setOtpCode(newOtpCode);
        user.setOtpExpiryTime(newExpiryTime);
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), newOtpCode);

        LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                .email(user.getEmail())
                .message("Mã OTP mới đã được gửi thành công đến email của bạn.")
                .build();

        outputPort.present(responseDTO);
    }

    // ==========================================
    // ➕ QUÊN MẬT KHẨU
    // ==========================================

    @Override
    public void forgotPassword(ForgotPasswordRequestDTO requestDTO) {
        Optional<User> userOpt = userRepository.findByEmail(requestDTO.getEmail());

        if (userOpt.isEmpty()) {
            outputPort.presentError("Không tìm thấy tài khoản tương ứng với email");
            return;
        }

        User user = userOpt.get();

        String otpCode = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(RESET_OTP_EXPIRY_MINUTES);

        user.setResetOtpCode(otpCode);
        user.setResetOtpExpiryTime(expiryTime);
        user.setResetOtpAttempts(0);
        user.setResetOtpLockedUntil(null);
        user.setResetToken(null);
        user.setResetTokenExpiryTime(null);
        userRepository.save(user);

        emailService.sendResetPasswordOtpEmail(user.getEmail(), otpCode);

        LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                .email(user.getEmail())
                .message("Mã OTP đặt lại mật khẩu đã được gửi đến email của bạn.")
                .build();

        outputPort.present(responseDTO);
    }

    @Override
    public void verifyResetOtp(VerifyResetOtpRequestDTO requestDTO) {
        Optional<User> userOpt = userRepository.findByEmail(requestDTO.getEmail());

        if (userOpt.isEmpty()) {
            outputPort.presentError("Không tìm thấy tài khoản tương ứng với email");
            return;
        }

        User user = userOpt.get();

        if (user.getResetOtpLockedUntil() != null && LocalDateTime.now().isBefore(user.getResetOtpLockedUntil())) {
            outputPort.presentError("Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau ít phút.");
            return;
        }

        if (user.getResetOtpCode() == null || user.getResetOtpExpiryTime() == null) {
            outputPort.presentError("Bạn chưa yêu cầu mã OTP đặt lại mật khẩu");
            return;
        }

        if (LocalDateTime.now().isAfter(user.getResetOtpExpiryTime())) {
            outputPort.presentError("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
            return;
        }

        if (!user.getResetOtpCode().equals(requestDTO.getOtpCode())) {
            int attempts = user.getResetOtpAttempts() + 1;
            user.setResetOtpAttempts(attempts);

            if (attempts >= MAX_RESET_OTP_ATTEMPTS) {
                user.setResetOtpLockedUntil(LocalDateTime.now().plusMinutes(RESET_OTP_LOCK_MINUTES));
                user.setResetOtpCode(null);
                userRepository.save(user);
                outputPort.presentError("Bạn đã nhập sai quá 5 lần. Tài khoản bị khoá xác thực trong 15 phút.");
                return;
            }

            userRepository.save(user);
            outputPort.presentError("Mã OTP không chính xác. Còn lại " + (MAX_RESET_OTP_ATTEMPTS - attempts) + " lần thử.");
            return;
        }

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiryTime(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES));
        user.setResetOtpCode(null);
        user.setResetOtpExpiryTime(null);
        user.setResetOtpAttempts(0);
        user.setResetOtpLockedUntil(null);
        userRepository.save(user);

        LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                .email(user.getEmail())
                .resetToken(resetToken)
                .message("Xác thực OTP thành công. Vui lòng đặt mật khẩu mới.")
                .build();

        outputPort.present(responseDTO);
    }

    @Override
    public void resetPassword(ResetPasswordRequestDTO requestDTO) {
        Optional<User> userOpt = userRepository.findByResetToken(requestDTO.getResetToken());

        if (userOpt.isEmpty()) {
            outputPort.presentError("Token đặt lại mật khẩu không hợp lệ");
            return;
        }

        User user = userOpt.get();

        if (user.getResetTokenExpiryTime() == null || LocalDateTime.now().isAfter(user.getResetTokenExpiryTime())) {
            outputPort.presentError("Token đặt lại mật khẩu đã hết hạn. Vui lòng thực hiện lại từ đầu.");
            return;
        }

        user.setPassword(passwordEncoder.encode(requestDTO.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiryTime(null);
        userRepository.save(user);

        LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                .email(user.getEmail())
                .message("Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại.")
                .build();

        outputPort.present(responseDTO);
    }
}