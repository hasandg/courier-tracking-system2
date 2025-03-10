package com.hasandag.courier.tracking.system.location.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:location-service}")
    private String applicationName;

    @Bean
    public OpenAPI locationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Location Service API")
                        .description("API for tracking courier locations")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Hasan Dag")
                                .email("hasan_email@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Default Server URL")
                ));
    }
} 