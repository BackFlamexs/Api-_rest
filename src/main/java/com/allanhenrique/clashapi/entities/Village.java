package com.allanhenrique.clashapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "tb_villages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Village {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID da vila", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "O nome da vila é obrigatório")
    @Schema(example = "Vila do Allan", description = "Nome da base do jogador")
    private String name;

    @NotNull(message = "O nível do Centro de Vila é obrigatório")
    @Min(value = 1, message = "O nível do Centro da Vila deve ser pelo menos 1")
    @Schema(example = "12", description = "Nível do Centro de Vila (CV)")
    private Integer townHallLevel;

    @NotNull(message = "A vila deve pertencer a um jogador")
    @OneToOne
    @JoinColumn(name = "player_id", unique = true)
    // CORREÇÃO 1: Ignora "village" ao serializar Player (quebra o loop JSON)
    // Ignora também "troops" e "spells" para evitar serialização de coleções lazy
    @JsonIgnoreProperties({"village", "troops", "spells"})
    // CORREÇÃO 2: Exclui o campo do toString() gerado pelo Lombok (evita StackOverflow)
    @ToString.Exclude
    // CORREÇÃO 3: Exclui o campo do equals/hashCode gerado pelo Lombok (evita loop infinito)
    @EqualsAndHashCode.Exclude
    @Schema(example = "{ \"id\": 1 }", description = "ID do jogador que já existe")
    private Player player;
}