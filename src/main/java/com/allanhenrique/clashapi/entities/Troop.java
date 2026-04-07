package com.allanhenrique.clashapi.entities;

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
    private Long id;

    @NotBlank(message = "O nome da tropa é obrigatório")
    private String name;

    @NotNull(message = "O dano é obrigatório")
    @Min(value = 0, message = "O dano não pode ser negativo")
    private Integer damage;

    // ManyToMany
    @ManyToMany(mappedBy = "troops")
    private Set<Player> players = new HashSet<>();
}