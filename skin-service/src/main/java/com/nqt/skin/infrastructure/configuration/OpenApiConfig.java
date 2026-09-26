package com.nqt.skin.infrastructure.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
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
    public OpenAPI skinOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Skin Service (Quản lý trang phục & Avatar nhân vật PENTAVA)")
                        .description("Quản lý kho đồ skin, trang bị / tháo trang phục và trả về các layer render avatar nhân vật theo thứ tự chuẩn")
                        .version("1.0"))
                .servers(List.of(new Server().url(serverUrl).description("API Gateway Server")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
