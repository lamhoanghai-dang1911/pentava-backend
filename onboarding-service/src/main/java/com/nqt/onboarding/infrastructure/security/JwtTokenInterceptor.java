package com.nqt.onboarding.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders; // ➕ Thêm import này
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.security.Key;

@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenInterceptor.class);

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String jwtSecret;

    private Key getSigningKey() {
        // ➕ Dùng chung cơ chế Decoders.BASE64 với auth-service
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = Jwts.parser()
                        .verifyWith((javax.crypto.SecretKey) getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String email = claims.getSubject();
                request.setAttribute("currentEmail", email);
            } catch (Exception e) {
                logger.error("Lỗi xác thực JWT Token: {}", e.getMessage(), e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token: " + e.getMessage());
                return false;
            }
        }
        return true;
    }
}