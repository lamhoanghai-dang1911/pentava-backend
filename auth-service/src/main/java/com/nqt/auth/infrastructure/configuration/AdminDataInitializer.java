package com.nqt.auth.infrastructure.configuration;

import com.nqt.auth.infrastructure.persistence.UserEntity;
import com.nqt.auth.interfaceadapters.gateway.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminDataInitializer.class);

    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminUsername = "pentava.official@gmail.com";

        userRepository.findByEmail(adminUsername)
                .or(() -> userRepository.findByEmail("administrator@pentava.online"))
                .ifPresentOrElse(
                        admin -> {
                            boolean needUpdate = false;
                            if (!"ROLE_ADMIN".equals(admin.getRole())) {
                                admin.setRole("ROLE_ADMIN");
                                needUpdate = true;
                            }
                            if (!admin.isVerified()) {
                                admin.setVerified(true);
                                needUpdate = true;
                            }
                            if (needUpdate) {
                                userRepository.save(admin);
                                log.info("Đã cập nhật role ROLE_ADMIN và trạng thái verified cho tài khoản admin '{}'", admin.getEmail());
                            }
                        },
                        () -> {
                            log.info("Khởi tạo tài khoản Administrator mặc định...");
                            UserEntity admin = UserEntity.builder()
                                    .email(adminUsername)
                                    .password(passwordEncoder.encode("Admin@123"))
                                    .name("Administrator")
                                    .role("ROLE_ADMIN")
                                    .authProvider("LOCAL")
                                    .isVerified(true)
                                    .hasCompletedOnboarding(true)
                                    .createdAt(LocalDateTime.now())
                                    .build();
                            userRepository.save(admin);
                            log.info("Tạo thành công tài khoản Administrator: '{}' / Role: ROLE_ADMIN", adminUsername);
                        }
                );
    }
}
