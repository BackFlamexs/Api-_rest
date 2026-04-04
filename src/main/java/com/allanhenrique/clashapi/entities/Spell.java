package com.allanhenrique.clashapi.entities;

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
    private Long id;

    @NotBlank(message = "O nome do feitiço é obrigatório")
    private String name;

    private String type; // Ex: Cura, Fúria, Salto

    @ManyToMany
    @JoinTable(
            name = "tb_player_spells",
            joinColumns = @JoinColumn(name = "spell_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private Set<Player> players = new HashSet<>();
}