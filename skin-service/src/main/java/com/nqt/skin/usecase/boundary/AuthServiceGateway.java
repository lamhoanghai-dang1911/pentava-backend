package com.nqt.skin.usecase.boundary;

import java.util.Optional;

public interface AuthServiceGateway {
    Optional<Long> getUserIdByEmail(String email);
}
