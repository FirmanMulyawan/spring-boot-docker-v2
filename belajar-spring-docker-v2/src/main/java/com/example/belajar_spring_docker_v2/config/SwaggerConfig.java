package com.example.belajar_spring_docker_v2.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Belajar Spring Boot API")
                        .version("1.0.0")
                        .description("API dokumentasi untuk project belajar Spring Boot + Docker + Redis")
                        .contact(new Contact()
                                .name("Firman Mulyawan")
                                .email("firman@gmail.com")));
    }
}
