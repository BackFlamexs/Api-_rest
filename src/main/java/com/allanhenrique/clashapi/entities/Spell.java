package com.allanhenrique.clashapi.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tb_spells")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Spell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID do feitiço", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "O nome do feitiço é obrigatório")
    @Schema(example = "Feitiço de Fúria", description = "Nome da magia")
    private String name;

    @NotBlank(message = "O tipo do feitiço é obrigatório")
    @Schema(example = "SUPORTE", description = "Tipo de efeito (DANO, SUPORTE, CURA, SALTO)")
    private String type; // Ex: Cura, Fúria, Salto

    @Schema(hidden = true)
    @ManyToMany
    @JoinTable(
            name = "tb_player_spells",
            joinColumns = @JoinColumn(name = "spell_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private Set<Player> players = new HashSet<>();
}