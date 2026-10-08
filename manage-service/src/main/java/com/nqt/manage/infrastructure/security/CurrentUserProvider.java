package com.nqt.manage.infrastructure.security;

import com.nqt.manage.usecase.boundary.AuthServiceGateway;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final HttpServletRequest request;
    private final AuthServiceGateway authServiceGateway;

    public String getCurrentUserEmail() {
        Object email = request.getAttribute("currentEmail");
        if (!(email instanceof String value) || value.isBlank()) {
            throw new IllegalStateException("Không tìm thấy thông tin xác thực người dùng");
        }
        return value;
    }

    public Optional<String> getOptionalCurrentUserEmail() {
        Object email = request.getAttribute("currentEmail");
        if (email instanceof String value && !value.isBlank()) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    public String getCurrentUserRole() {
        Object role = request.getAttribute("currentRole");
        return role instanceof String s ? s : "";
    }

    public boolean isAdmin() {
        String role = getCurrentUserRole();
        return "ROLE_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }

    public Long resolveCurrentUserId() {
        String email = getCurrentUserEmail();
        return authServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Không thể xác định User ID từ email: " + email));
    }

    public Optional<Long> getOptionalCurrentUserId() {
        return getOptionalCurrentUserEmail()
                .flatMap(authServiceGateway::getUserIdByEmail);
    }
}
