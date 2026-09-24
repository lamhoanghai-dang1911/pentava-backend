package com.nqt.cinema.infrastructure.security;

import com.nqt.cinema.usecase.exception.CinemaBusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    private final HttpServletRequest request;

    public CurrentUserProvider(HttpServletRequest request) {
        this.request = request;
    }

    /** Email đã được JwtTokenInterceptor giải mã và gán vào request. */
    public String getCurrentUserEmail() {
        Object email = request.getAttribute("currentEmail");
        if (!(email instanceof String value) || value.isBlank()) {
            throw new CinemaBusinessException("Không tìm thấy thông tin đăng nhập");
        }
        return value;
    }
}