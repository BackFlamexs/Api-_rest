package com.allanhenrique.clashapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta da versão 2 do clã — inclui total de membros")
public class ClanV2Response {

    @Schema(description = "ID do clã")
    private Long id;

    @Schema(example = "Clã dos Programadores", description = "Nome do clã")
    private String name;

    @Schema(example = "Focado em Guerras e Doações", description = "Descrição do clã")
    private String description;

    @Schema(example = "2000", description = "Troféus mínimos para entrar")
    private Integer requiredTrophies;

    @Schema(example = "true", description = "Privacidade do Clã")
    private Boolean isPublic;

    @Schema(description = "Total de jogadores membros do clã — novo campo da v2")
    private int totalMembers;
}