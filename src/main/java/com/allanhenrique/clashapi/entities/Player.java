package com.allanhenrique.clashapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tb_players")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nickname não pode ser vazio")
    @Size(min = 3, max = 20, message = "O nickname deve ter entre 3 e 20 caracteres")
    private String nickname;

    @Min(value = 1, message = "O nível mínimo é 1")
    @Max(value = 300, message = "O nível máximo permitido é 300")
    private Integer level;

    // REQUISITO: Implementar um Enum
    @Enumerated(EnumType.STRING)
    @NotNull(message = "O cargo role é obrigatório")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "clan_id")
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
}