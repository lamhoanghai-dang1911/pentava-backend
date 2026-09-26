package com.nqt.shop.usecase.boundary;

import java.util.Optional;

public interface AuthServiceGateway {
    Optional<Long> getUserIdByEmail(String email);
}
