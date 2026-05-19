package com.allanhenrique.clashapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tb_troops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Troop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID da tropa", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "O nome da tropa é obrigatório")
    @Schema(example = "Dragão Elétrico", description = "Nome da unidade militar")
    private String name;

    @NotNull(message = "O dano é obrigatório")
    @Min(value = 0, message = "O dano não pode ser negativo")
    @Schema(example = "1050", description = "Dano por segundo (DPS)")
    private Integer damage;

    @Schema(example = "FOGO", description = "Tipo de dano causado pela tropa (FOGO, GELO, AR, TERRA)")
    private String damageType;

    public String getDamageType() {
        return damageType;
    }

    public void setDamageType(String damageType) {
        this.damageType = damageType;
    }

    // CORREÇÃO: @JsonIgnore evita loop infinito Troop → players → troops → players...
    @JsonIgnore
    @Schema(hidden = true)
    @ManyToMany(mappedBy = "troops")
    private Set<Player> players = new HashSet<>();
}