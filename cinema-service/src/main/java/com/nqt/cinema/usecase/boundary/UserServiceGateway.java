package com.nqt.cinema.usecase.boundary;

import java.util.Optional;

public interface UserServiceGateway {
    Optional<Long> getUserIdByEmail(String email);
}