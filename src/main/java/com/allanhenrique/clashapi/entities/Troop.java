package com.allanhenrique.clashapi.entities;

import jakarta.persistence.*;
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

    private String name;

    private Integer damage;

    // ManyToMany
    @ManyToMany(mappedBy = "troops")
    private Set<Player> players = new HashSet<>();
}