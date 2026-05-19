package com.diploma.Diplom.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("LMS Diploma API")
                        .version("1.0.0")
                        .description("""
                                Learning Management System REST API.
                                
                                **Authentication:** Most endpoints require a JWT token.
                                1. Call `POST /auth/login` to get your token.
                                2. Click the **Authorize** button at the top right.
                                3. Enter `Bearer <your_token>` and confirm.
                                
                                **Roles:** STUDENT · TEACHER · ADMIN
                                """)
                        .contact(new Contact()
                                .name("Diploma Project")
                                .email("rkulzabaj@gmail.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local dev")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT token returned by /auth/login")));
    }
}