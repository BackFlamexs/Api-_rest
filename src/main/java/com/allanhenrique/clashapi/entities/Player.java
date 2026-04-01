package com.allanhenrique.clashapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Long id;

    @NotBlank(message = "O nickname é obrigatório")
    private String nickname;

    private Integer level;

    // relacionamento de muitos jogadores para um clan
    @ManyToOne
    @JoinColumn(name = "clan_id")
    private Clan clan;

    // relacionamento de muitos jogadores para muitos tropas
    @ManyToMany
    @JoinTable(
            name = "tb_player_troops",
            joinColumns = @JoinColumn(name = "player_id"),
            inverseJoinColumns = @JoinColumn(name = "troop_id")
    )
    private Set<Troop> troops = new HashSet<>();
}