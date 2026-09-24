package com.nqt.cinema.infrastructure.configuration;

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

/**
 * Thông tin hiển thị trên Swagger và nút "Authorize" (Bearer JWT).
 * Các API của cinema-service đều cần token nên không có nút này thì không thể thử trên Swagger.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    /**
     * Địa chỉ mà Swagger dùng để gọi API. Mặc định trỏ về API Gateway (8080) thay vì
     * cinema-service:8085 (địa chỉ nội bộ trong mạng Docker, trình duyệt không truy cập được).
     * Khi deploy có thể đổi bằng biến môi trường OPENAPI_SERVER_URL, ví dụ https://pentava.online
     */
    @Value("${openapi.server-url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI cinemaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cinema Service (PENTA-CINEMA)")
                        .description("Tổng hợp ảnh check-in trong tuần thành clip")
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