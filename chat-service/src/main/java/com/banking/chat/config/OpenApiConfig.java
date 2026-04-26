package com.banking.chat.config;

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

@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Bean
    public OpenAPI chatServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Chat Service API")
                        .description("Multi-turn chat service with context-aware conversation management and session persistence")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Banking Platform Team")
                                .email("support@bankingplatform.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://bankingplatform.com/license")))
                .servers(List.of(
                        new Server().url("http://localhost:8086" + contextPath).description("Development Server"),
                        new Server().url("https://staging.bankingplatform.com" + contextPath).description("Staging Server"),
                        new Server().url("https://api.bankingplatform.com" + contextPath).description("Production Server")
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
