package com.nqt.auth.infrastructure.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.nqt.auth.usecase.boundary.GoogleTokenGateway;
import com.nqt.auth.usecase.dto.GoogleUserInfo;
import com.nqt.auth.usecase.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
public class GoogleTokenVerifierAdapter implements GoogleTokenGateway {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifierAdapter(GoogleIdTokenVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public GoogleUserInfo verify(String idTokenString) {
        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(idTokenString);
        } catch (GeneralSecurityException | IOException e) {
            throw new BusinessException("Không thể xác thực Google idToken. Vui lòng thử lại.");
        }

        if (idToken == null) {
            throw new BusinessException("Google idToken không hợp lệ hoặc đã hết hạn.");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
        if (!emailVerified) {
            throw new BusinessException("Email Google của bạn chưa được xác thực.");
        }

        String email = payload.getEmail();
        String sub = payload.getSubject();
        String name = (String) payload.get("name");

        return new GoogleUserInfo(sub, email, true, name);
    }
}