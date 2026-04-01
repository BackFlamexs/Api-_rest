package com.allanhenrique.clashapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_villages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Village {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Min(value = 1, message = "O nível do Centro da Vila deve ser pelo menos 1")
    private Integer townHallLevel;

    // Relacionamento de uma vila para um jogador, o jogador somente pode ter uma vila naquela conta de id e caso ele queira ter outra vila tera que criar outra conta ccom outro ID
    @OneToOne
    @JoinColumn(name = "player_id", unique = true)
    private Player player;
}