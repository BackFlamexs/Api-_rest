package com.allanhenrique.clashapi.infrastructure;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Nome do esquema de segurança — precisa ser igual ao usado no @SecurityRequirement
        final String securitySchemeName = "X-API-Key";

        return new OpenAPI()
                .info(new Info()
                        .title("Clash API - Gerenciamento de Vilas e Tropas")
                        .version("3.0.0")
                        .description("API RESTful desenvolvida para o gerenciamento completo de um ecossistema estilo Clash. " +
                                "\n\nEsta documentação oferece os endpoints necessários para administrar Clãs, Jogadores, Vilas, Tropas e Feitiços." +
                                "\n\n**Autenticação:** Gere uma chave em `POST /api-keys` e use-a no botão **Authorize** acima.")
                )

                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Chave de API gerada em POST /api-keys. Cole aqui para autenticar todas as requisições.")
                        )
                )
                // Aplica a segurança globalmente em todos os endpoints
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}