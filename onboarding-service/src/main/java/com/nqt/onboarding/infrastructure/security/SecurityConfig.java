package com.nqt.onboarding.infrastructure.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**",           // Cho phép truy cập các file giao diện tĩnh của Swagger
                                "/api/onboarding/**"     // Cho phép gọi API onboarding trực tiếp
                        ).permitAll()
                        .anyRequest().permitAll()    // Mở toàn bộ cho môi trường nội bộ/microservice
                );
        return http.build();
    }
}
