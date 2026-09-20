package com.nqt.checkin.infrastructure.security;

import com.nqt.checkin.usecase.exception.CheckinBusinessException;
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
            throw new CheckinBusinessException("Không tìm thấy thông tin đăng nhập");
        }
        return value;
    }
}