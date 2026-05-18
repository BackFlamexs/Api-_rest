package com.allanhenrique.clashapi.infrastructure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "X-API-Key";

        return new OpenAPI()
                .info(new Info()
                        .title("Clash API - Gerenciamento de Vilas e Tropas")
                        .version("3.0.0")
                        .description("""
                                A **Clash API** é um serviço RESTful robusto, projetado para gerenciar as regras de negócio e a persistência de dados de um ecossistema de jogo estratégico.
                                
                                Desenvolvida com foco em escalabilidade e consistência, a arquitetura garante a integridade das transações e oferece uma interface padronizada para integração com clientes web e mobile.
                                
                                ---
                                
                                ### 📌 Domínios de Negócio
                                * **Perfis e Alianças:** Gerenciamento centralizado de Jogadores (níveis, troféus e estatísticas) e sua alocação dinâmica em Clãs.
                                * **Gestão Territorial (Vilas):** Controle estrutural rigoroso das bases. O sistema valida regras de negócio avançadas, assegurando a relação de cardinalidade 1:1 absoluta entre um Jogador e sua respectiva Vila.
                                * **Catálogo Militar:** Administração dos artefatos de batalha, centralizando o registro e o balanceamento de Tropas e Feitiços disponíveis no ecossistema.
                                
                                ### ⚙️ Arquitetura e Engenharia
                                * **Transações Idempotentes:** Rotas de manipulação crítica exigem o cabeçalho `X-Idempotency-Key`. Este mecanismo de engenharia assegura que falhas de rede ou múltiplas requisições simultâneas não gerem duplicidade de dados (`409 Conflict`).
                                * **Serialização e Resiliência:** A modelagem de dados JPA foi estruturada com blindagem nas entidades bidirecionais. Isso otimiza o processamento JSON, prevenindo dependências circulares e anomalias de memória no servidor.
                                * **Controle de Acesso:** A comunicação opera sob um protocolo de segurança customizado baseado em `API Keys`, restringindo o acesso aos recursos apenas a clientes devidamente autorizados.
                                
                                ---
                                
                                ### 🔒 Instruções de Acesso
                                1. Realize a chamada ao endpoint `POST /api-keys` para provisionar a sua credencial de consumo.
                                2. Copie o token gerado no corpo da resposta.
                                3. Utilize o botão **Authorize** (ícone de cadeado) localizado no cabeçalho desta documentação para injetar a chave automaticamente nas requisições subsequentes.
                                """)
                        .contact(new Contact()
                                .name("BackFlamexs")
                                .url("https://github.com/BackFlamexs"))
                )

                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Insira sua API Key gerada via POST /api-keys para autenticação global.")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}