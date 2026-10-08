package com.nqt.manage.infrastructure.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Value("${openapi.server-url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI manageServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Manage Service API - PENTAVA")
                        .description("API quản lý người dùng và tiếp nhận thông tin liên hệ / góp ý website PENTAVA.")
                        .version("1.0")
                        .contact(new Contact().name("PENTAVA Team").email("support@pentava.online")))
                .servers(List.of(new Server().url(serverUrl).description("API Gateway Server")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}

