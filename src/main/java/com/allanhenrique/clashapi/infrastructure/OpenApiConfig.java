package com.allanhenrique.clashapi.infrastructure;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Clash API - Gerenciamento de Vilas e Tropas")
                        .version("1.0.0")
                        .description("API RESTful desenvolvida para o gerenciamento completo de um ecossistema estilo Clash. " +
                                "\n\nEsta documentação oferece os endpoints necessários para administrar Clãs, Jogadores, Vilas, Tropas e Feitiços.")
                );
    }
}