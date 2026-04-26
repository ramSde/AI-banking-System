package com.banking.rag.config;

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
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RAG Pipeline Service API")
                        .version("1.0.0")
                        .description("RAG (Retrieval-Augmented Generation) Pipeline Service for intelligent document retrieval, reranking, context assembly, and source attribution")
                        .contact(new Contact()
                                .name("Banking Platform Team")
                                .email("support@banking-platform.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://banking-platform.com/license")))
                .servers(List.of(
                        new Server().url("http://localhost:8093" + contextPath).description("Development Server"),
                        new Server().url("https://staging.banking-platform.com" + contextPath).description("Staging Server"),
                        new Server().url("https://api.banking-platform.com" + contextPath).description("Production Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT authentication token")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
