package com.nqt.shop.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.SecretKey;

@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenInterceptor.class);

    private final SecretKey signingKey;

    public JwtTokenInterceptor(@Value("${jwt.secret}") String jwtSecret) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();

        boolean isPublic = uri.startsWith("/v3/api-docs") ||
                uri.startsWith("/swagger-ui") ||
                uri.startsWith("/api/shop/webhook") ||
                ("GET".equalsIgnoreCase(request.getMethod()) && (
                        uri.equals("/api/shop/items") ||
                        uri.equals("/api/shop/catalog") ||
                        uri.equals("/api/shop/topup/packages") ||
                        uri.equals("/api/shop/subscription/plans")
                )) ||
                uri.startsWith("/api/shop/topup/mock-confirm") ||
                uri.startsWith("/api/shop/subscription/mock-confirm") ||
                uri.startsWith("/api/shop/subscription/entitlement");

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(signingKey)
                        .build()
                        .parseSignedClaims(authHeader.substring(7))
                        .getPayload();

                String email = claims.getSubject();
                String role = claims.get("role", String.class);

                if (email != null && !email.isBlank()) {
                    request.setAttribute("currentEmail", email);
                    request.setAttribute("currentRole", role);

                    // Nếu là endpoint admin thì yêu cầu quyền ADMIN
                    if (uri.startsWith("/api/shop/admin/")) {
                        if (!"ROLE_ADMIN".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
                            log.warn("Truy cập bị từ chối: Tài khoản '{}' có role='{}' không có quyền ADMIN trên URI {}", email, role, uri);
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Quyền truy cập chỉ dành riêng cho Quản trị viên (ADMIN)");
                            return false;
                        }
                    }

                    return true;
                }
            } catch (Exception e) {
                log.warn("JWT không hợp lệ: {}", e.getMessage());
                if (!isPublic) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                    return false;
                }
            }
        }

        if (isPublic) {
            return true;
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
        return false;
    }
}
