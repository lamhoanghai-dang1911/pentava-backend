package com.nqt.checkin.usecase.boundary;

import java.util.Optional;

public interface UserServiceGateway {
    Optional<Long> getUserIdByEmail(String email);
}