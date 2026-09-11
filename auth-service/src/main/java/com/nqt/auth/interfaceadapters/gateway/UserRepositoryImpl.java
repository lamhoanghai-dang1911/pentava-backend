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
                .createdAt(user.getCreatedAt())
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
                .createdAt(entity.getCreatedAt())
                .build();
    }
}