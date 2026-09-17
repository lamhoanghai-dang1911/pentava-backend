package com.nqt.auth.infrastructure.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class GoogleAuthConfig {

    @Value("${google.oauth.client-ids}")
    private String clientIdsProperty;

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        List<String> audiences = Arrays.stream(clientIdsProperty.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (audiences.isEmpty()) {
            throw new IllegalStateException(
                    "google.oauth.client-ids chưa được cấu hình. Vui lòng set biến môi trường GOOGLE_OAUTH_CLIENT_IDS.");
        }

        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(audiences)
                .build();
    }
}