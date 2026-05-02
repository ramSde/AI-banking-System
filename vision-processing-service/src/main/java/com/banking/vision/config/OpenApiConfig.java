package com.banking.vision.config;

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
 * OpenAPI 3.0 configuration for Vision Processing Service.
 * 
 * Provides interactive API documentation via Swagger UI.
 * 
 * Access:
 * - Swagger UI: http://localhost:8018/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8018/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:vision-processing-service}")
    private String applicationName;

    @Value("${server.port:8018}")
    private String serverPort;

    @Bean
    public OpenAPI visionProcessingOpenAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("Local development server"),
                new Server()
                    .url("https://api-staging.banking.example.com")
                    .description("Staging server"),
                new Server()
                    .url("https://api.banking.example.com")
                    .description("Production server")
            ))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", securityScheme())
            )
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    private Info apiInfo() {
        return new Info()
            .title("Vision Processing Service API")
            .description("""
                OCR and Document Intelligence Service for Banking Platform
                
                ## Features
                - Document upload and processing
                - OCR text extraction (Tesseract)
                - Structured data extraction
                - Support for receipts, invoices, checks, bank statements, ID documents
                - Async processing for large documents
                - Template-based extraction rules
                
                ## Document Types
                - **RECEIPT**: Extract merchant, date, total, line items
                - **INVOICE**: Extract vendor, invoice number, line items, totals
                - **BANK_STATEMENT**: Extract transactions, balances
                - **CHECK**: Extract routing number, account number, amount
                - **ID_DOCUMENT**: Extract name, DOB, ID number (KYC)
                - **GENERIC**: Full text extraction
                
                ## Authentication
                All endpoints require JWT Bearer token authentication.
                Include token in Authorization header: `Bearer <token>`
                
                ## Rate Limits
                - User: 100 requests/minute
                - IP: 200 requests/minute
                
                ## File Limits
                - Max file size: 10MB
                - Supported formats: PDF, PNG, JPG, JPEG, TIFF
                """)
            .version("1.0.0")
            .contact(new Contact()
                .name("Banking Platform Team")
                .email("support@banking.example.com")
                .url("https://banking.example.com"))
            .license(new License()
                .name("Proprietary")
                .url("https://banking.example.com/license"));
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("JWT authentication token. Obtain from Identity Service.");
    }
}
