package com.nqt.auth.infrastructure.configuration;

import com.nqt.auth.infrastructure.security.JwtTokenProvider;
import com.nqt.auth.interfaceadapters.gateway.UserRepositoryImpl;
import com.nqt.auth.interfaceadapters.presenter.AuthPresenter;
import com.nqt.auth.infrastructure.service.EmailService;
import com.nqt.auth.usecase.interactor.AuthInteractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public AuthInteractor authInteractor(UserRepositoryImpl userRepository,
                                         AuthPresenter authPresenter,
                                         EmailService emailService,
                                         JwtTokenProvider jwtTokenProvider) {
        return new AuthInteractor(userRepository, authPresenter, emailService, jwtTokenProvider);
    }
}