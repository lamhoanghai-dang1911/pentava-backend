package com.nqt.auth.usecase.interactor;
import com.nqt.auth.domain.User;
import java.util.Optional;

public interface UserRepositoryGateway {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    User save(User user);
}