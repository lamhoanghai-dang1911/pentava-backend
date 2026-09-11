package com.nqt.onboarding.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {
    private final HttpServletRequest request;

    public CurrentUserProvider(HttpServletRequest request) {
        this.request = request;
    }

    public String getCurrentUserEmail() {
        // Lấy trực tiếp chuỗi email đã được Interceptor giải mã và gán vào attribute
        String email = (String) request.getAttribute("currentEmail");

        if (email == null || email.isEmpty()) {
            throw new RuntimeException("Không tìm thấy thông tin email từ Token hoặc chưa đăng nhập!");
        }

        return email;
    }

    public Long getCurrentUserId() {
        try {
            Jwt jwt = (Jwt) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();

            // ➕ SỬA: Dùng getClaimAsString rồi convert sang Long
            String userIdStr = jwt.getClaimAsString("userId");
            if (userIdStr != null && !userIdStr.isEmpty()) {
                return Long.parseLong(userIdStr);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}