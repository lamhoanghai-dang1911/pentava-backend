package com.nqt.shop.usecase.boundary;

import com.nqt.shop.usecase.dto.UserDetailDTO;
import java.util.Optional;

public interface AuthServiceGateway {
    Optional<Long> getUserIdByEmail(String email);
    Optional<UserDetailDTO> getUserById(Long userId);
}
