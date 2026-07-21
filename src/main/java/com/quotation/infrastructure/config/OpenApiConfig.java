package com.quotation.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI quotationApiDocs() {
        return new OpenAPI()
                .info(new Info()
                        .title("Quotation System API")
                        .version("1.3.0")
                        .description("""
                                REST API for quotation management with volume discounts, business validations, \
                                JWT authentication, role-based access control, and PDF export.

                                ## Overview
                                This API handles the full lifecycle of a quotation:
                                managing clients (Users), building a product catalogue (Products),
                                generating itemised quotations with automatic price calculation (Quotations),
                                and exporting quotations as PDF documents.

                                ## Architecture
                                Built with **Hexagonal Architecture** (Ports & Adapters):
                                - `domain/` — pure business logic, zero Spring dependencies
                                - `application/` — use cases, DTOs, port interfaces
                                - `infrastructure/` — REST controllers, JPA adapters, PDF generation, configuration

                                ## Status codes
                                | Code | Meaning |
                                |------|---------|
                                | 200  | Success |
                                | 201  | Resource created |
                                | 204  | Success, no content (DELETE) |
                                | 400  | Validation error |
                                | 401  | Unauthorized — missing or invalid JWT |
                                | 403  | Forbidden — insufficient role |
                                | 404  | Resource not found |
                                | 409  | Conflict (e.g. duplicate email) |
                                | 500  | Unexpected server error |
                                """)
                        .contact(new Contact()
                                .name("Borja Rodríguez")
                                .email("borja.rodriguez789@gmail.com")
                                .url("https://github.com/borja8dev/quotation-system-api"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development")))
                .tags(List.of(
                        new Tag()
                                .name("Users")
                                .description("Manage clients — create, read, update, and delete user accounts"),
                        new Tag()
                                .name("Products")
                                .description("Manage the product catalogue — boards, fittings, profiles, modules, and services"),
                        new Tag()
                                .name("Quotations")
                                .description("Create and manage quotations with automatic line-total and subtotal calculation")));
    }
}
