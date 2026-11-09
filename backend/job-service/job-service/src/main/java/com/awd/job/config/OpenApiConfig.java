package com.awd.job.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi(@Value("${server.port:8082}") String port) {
        return new OpenAPI()
                .info(new Info()
                        .title("AWD Job Service")
                        .version("1.0.0")
                        .description("Job and category management (microservice).")
                        .contact(new Contact().name("Badia Abouhdid")))
                .servers(List.of(new Server().url("http://localhost:" + port).description("Local")));
    }
}
