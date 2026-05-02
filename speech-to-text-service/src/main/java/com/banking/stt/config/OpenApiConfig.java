package com.banking.stt.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) configuration for API documentation.
 * Provides interactive API documentation at /swagger-ui.html
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8019}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Speech-to-Text Service API")
                        .version("1.0.0")
                        .description("Audio transcription and speech recognition service for banking platform")
                        .contact(new Contact()
                                .name("Banking Platform Team")
                                .email("support@banking-platform.com")
                                .url("https://banking-platform.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://banking-platform.com/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort + "/api")
                                .description("Local Development"),
                        new Server()
                                .url("https://api-staging.banking-platform.com")
                                .description("Staging Environment"),
                        new Server()
                                .url("https://api.banking-platform.com")
                                .description("Production Environment")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT authentication token")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
