package com.allanhenrique.clashapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tb_players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID do jogador", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "O nickname não pode ser vazio")
    @Size(min = 3, max = 20, message = "O nickname deve ter entre 3 e 20 caracteres")
    @Schema(example = "Allan_Pro", description = "Nome de exibição no jogo")
    private String nickname;

    @NotNull(message = "O nível é obrigatório")
    @Min(value = 1, message = "O nível mínimo é 1")
    @Max(value = 300, message = "O nível máximo permitido é 300")
    @Schema(example = "75", description = "Nível de experiência atual")
    private Integer level;

    //implementar um Enum
    @Enumerated(EnumType.STRING)
    @NotNull(message = "O cargo role é obrigatório")
    @Schema(example = "LIDER", description = "Cargo hierárquico no clã")
    private Role role;

    @NotNull(message = "O jogador deve pertencer a um clã")
    @ManyToOne // Remova o cascade daqui!
    @JoinColumn(name = "clan_id")
    @Schema(description = "Vínculo com o clã")
    private Clan clan;

    @ManyToMany
    @JoinTable(
            name = "tb_player_troops",
            joinColumns = @JoinColumn(name = "player_id"),
            inverseJoinColumns = @JoinColumn(name = "troop_id")
    )
    private Set<Troop> troops = new HashSet<>();

    // relacao com a ultima entidade que eu estou criando
    @ManyToMany(mappedBy = "players")
    private Set<Spell> spells = new HashSet<>();

    // relacao de 1 para 1 com a vila que apaga a vila caso nao tenha um player vinculado a ela, no swagger precisa vincular a vila a algum player na hora de criacao.
    @JsonIgnore
    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private Village village;
}