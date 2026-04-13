package com.allanhenrique.clashapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Table(name = "tb_clans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Clan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID do clã", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "O nome do clã é obrigatório")
    @Size(min = 3, max = 50, message = "O nome deve ter entre 3 e 50 caracteres")
    @Column(nullable = false, unique = true)
    @Schema(example = "Clã dos Programadores", description = "Nome único do clã")
    private String name;

    @NotBlank(message = "A descrição é obrigatória")
    @Column(length = 500)
    @Schema(example = "Focado em Guerras e Doações", description = "Descrição dos objetivos")
    private String description;

    @NotNull(message = "A quantidade de troféus é obrigatória")
    @Min(value = 0, message = "Os troféus necessários não podem ser negativos")
    @Column(nullable = false)
    @Schema(example = "2000", description = "Troféus mínimos para ingressar")
    private Integer requiredTrophies;

    @JsonIgnore
    @OneToMany(mappedBy = "clan")
    private List<Player> players;
}