package com.app.trlapp.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.security.SecurityRequirement;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("My API")
                        .version("1.0")
                        .description("API documentation"))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))  // Use basicAuth for security
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("basicAuth", createBasicAuthScheme()))  // Define basicAuth scheme
                .servers(List.of(new io.swagger.v3.oas.models.servers.Server()
                        .url(getApiDocsPath())));  // Set API docs path dynamically
    }

    // Method to create basic auth security scheme
    private SecurityScheme createBasicAuthScheme() {
        return new SecurityScheme()
                .type(Type.HTTP)
                .scheme("basic")  // Set the scheme to basic
                .description("Basic authentication with username and password");
    }

    // Method to get the API docs path from the environment variable
    private String getApiDocsPath() {
        return System.getenv("API_PATH");  // Get API_PATH from environment variables
    }
}
