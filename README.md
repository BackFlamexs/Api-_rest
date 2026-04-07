# ClashAPI - Sistema de Gerenciamento de Ecossistema de Jogos

Este projeto consiste em uma API robusta de gerenciamento para elementos do universo Clash, desenvolvida com o que há de mais moderno no ecossistema Java. O foco principal foi a implementação de uma arquitetura limpa, seguindo os princípios RESTful e alcançando o Nível 3 da Maturidade de Richardson através de Hipermídia (HATEOAS).

## Stack Tecnológica e Inovação

O projeto utiliza a tecnologia de ponta do desenvolvimento backend:

* Linguagem: Java 25 (Utilizando as últimas JEPs para performance otimizada).
* Framework: Spring Boot 3.4.x.
* Persistência: Spring Data JPA com Hibernate 6.
* Banco de Dados: H2 Database (Memória) para agilidade em ambiente de desenvolvimento.
* Documentação: OpenAPI 3.0 (Swagger UI) para exploração interativa dos endpoints.
* HATEOAS: Navegação dinâmica por meio de links de hipermídia.
* Segurança e Validação: Jakarta Bean Validation (RFC 303) para integridade de dados.
* Produtividade: Project Lombok para redução de boilerplate.

## Modelagem e Relacionamentos (JPA)

A estrutura de dados foi projetada para suportar relacionamentos complexos e garantir a integridade referencial:

* Clan: Entidade central com regras de negócio para troféus necessários.
* Player: Gerenciamento de estados via Enums (LIDER, CO_LIDER, MEMBRO, ANCIAO).
* Village: Relacionamento 1:1 com o Player, utilizando CascadeType.ALL para sincronização de ciclo de vida.
* Troop & Spell: Catálogo de unidades militares com relacionamentos Many-to-Many, permitindo coleções dinâmicas por jogador.

## HATEOAS e Navegabilidade

A API não apenas entrega dados, mas guia o cliente através do nó _links. Cada recurso retornado (como um Jogador ou Clã) informa quais são as ações permitidas a partir daquele estado, como edição, deleção ou retorno à listagem principal.

## Tratamento de Erros (Resilience)

Implementação de um Global Exception Handler utilizando @ControllerAdvice para padronizar respostas de erro (RFC 7807):
* 404 (Not Found): Interceptação de IDs inexistentes com mensagens customizadas.
* 400 (Bad Request): Validação de sintaxe JSON e violações de integridade de banco de dados.
* 500 (Internal Error): Tratamento de falhas críticas com logging detalhado no servidor.

## Principais Endpoints

| Método | Endpoint | Função |
| :--- | :--- | :--- |
| GET | /players | Listagem paginada e ordenada. |
| POST | /players | Criação de jogador com vínculo a Clã existente. |
| PUT | /villages/{id} | Edição completa de vilas e layouts. |
| GET | /players/search | Query personalizada com ContainingIgnoreCase. |

## Como Executar

1. Pré-requisitos: JDK 25 e Maven.
2. Clone o projeto: git clone https://github.com/allanhenrique/clashapi.git
3. Build: mvn clean install
4. Run: mvn spring-boot:run
5. Acesse o Swagger: http://localhost:8080/swagger-ui.html

---
Desenvolvido por Allan Henrique
Estudante de Engenharia de Software | Backend Developer