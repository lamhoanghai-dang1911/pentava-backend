package com.nqt.manage.infrastructure.security;

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
        String method = request.getMethod();

        // Các endpoint công khai không yêu cầu token:
        boolean isPublic = uri.startsWith("/v3/api-docs") ||
                uri.startsWith("/swagger-ui") ||
                uri.equals("/api/manage/landing/overview") ||
                (uri.equals("/api/manage/contacts") && "POST".equalsIgnoreCase(method)) ||
                (uri.equals("/api/manage/newsletters") && "POST".equalsIgnoreCase(method)) ||
                (uri.equals("/api/manage/configs/public") && "GET".equalsIgnoreCase(method)) ||
                ("GET".equalsIgnoreCase(method) && (
                        uri.startsWith("/api/manage/banners") ||
                        uri.startsWith("/api/manage/features") ||
                        uri.startsWith("/api/manage/testimonials") ||
                        uri.startsWith("/api/manage/faqs") ||
                        uri.startsWith("/api/manage/pricing") ||
                        uri.startsWith("/api/manage/metrics")
                ));

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(signingKey)
                        .build()
                        .parseSignedClaims(authHeader.substring(7))
                        .getPayload();

                String email = claims.getSubject();
                if (email != null && !email.isBlank()) {
                    request.setAttribute("currentEmail", email);
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
