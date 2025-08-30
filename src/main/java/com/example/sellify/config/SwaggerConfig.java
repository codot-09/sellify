package com.example.sellify.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sellify API")
                        .version("1.0.0")
                        .description("Sellify — Telegram orqali ishlovchi online bozor backend API hujjati.")
                        .contact(new Contact()
                                .name("Sellify Team")
                                .url("https://github.com/codot-09/sellify")
                                .email("support@sellify.uz"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
