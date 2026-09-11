package com.nqt.onboarding.infrastructure.configuration;

import jakarta.annotation.PostConstruct; // 👈 Import cái này
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class GroqConfig {

    @Value("${groq.api-key}")
    private String apiKey;

    @Value("${groq.model:openai/gpt-oss-120b}")
    private String model;

    @PostConstruct
    public void init() {
        // 🔍 Đoạn này sẽ in key ra console khi app vừa start để bạn kiểm tra
        System.out.println("DEBUG GROQ API KEY LOADED: [" + apiKey + "]");
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }
}