package com.nqt.auth.interfaceadapters.gateway;

import com.nqt.auth.domain.User;
import com.nqt.auth.infrastructure.persistence.UserEntity;
import com.nqt.auth.usecase.interactor.UserRepositoryGateway;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepositoryGateway {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryImpl(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByResetToken(String resetToken) {
        return jpaRepository.findByResetToken(resetToken).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .name(user.getName())
                .authProvider(user.getAuthProvider())
                .otpCode(user.getOtpCode())
                .otpExpiryTime(user.getOtpExpiryTime())
                .isVerified(user.isVerified())
                .hasCompletedOnboarding(user.isHasCompletedOnboarding())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .resetOtpCode(user.getResetOtpCode())
                .resetOtpExpiryTime(user.getResetOtpExpiryTime())
                .resetOtpAttempts(user.getResetOtpAttempts())
                .resetOtpLockedUntil(user.getResetOtpLockedUntil())
                .resetToken(user.getResetToken())
                .resetTokenExpiryTime(user.getResetTokenExpiryTime())
                .build();

        UserEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    private User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .name(entity.getName())
                .authProvider(entity.getAuthProvider())
                .otpCode(entity.getOtpCode())
                .otpExpiryTime(entity.getOtpExpiryTime())
                .isVerified(entity.isVerified())
                .hasCompletedOnboarding(entity.isHasCompletedOnboarding())
                .avatarUrl(entity.getAvatarUrl())
                .bio(entity.getBio())
                .createdAt(entity.getCreatedAt())
                .resetOtpCode(entity.getResetOtpCode())
                .resetOtpExpiryTime(entity.getResetOtpExpiryTime())
                .resetOtpAttempts(entity.getResetOtpAttempts())
                .resetOtpLockedUntil(entity.getResetOtpLockedUntil())
                .resetToken(entity.getResetToken())
                .resetTokenExpiryTime(entity.getResetTokenExpiryTime())
                .build();
    }
}