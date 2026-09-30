package com.nqt.manage.usecase.boundary;

import com.nqt.manage.domain.model.AdminUser;

import java.util.Optional;

public interface AuthServiceGateway {
    Optional<AdminUser> getUserById(Long userId);
    Optional<AdminUser> getUserByEmail(String email);
    Optional<Long> getUserIdByEmail(String email);
}
