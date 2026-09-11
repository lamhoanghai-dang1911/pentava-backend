package com.nqt.checkin.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)     // ❌ Tắt hoàn toàn trang login mặc định
                .httpBasic(AbstractHttpConfigurer::disable)     // ❌ Tắt HTTP Basic authentication
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()               // ✅ Cho phép tất cả request đi qua không cần kiểm tra
                );
        return http.build();
    }
}