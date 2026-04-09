package com.allanhenrique.clashapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Table(name = "tb_clans")
@Getter
@Setter
@NoArgsConstructor // Cria o construtor vazio
@AllArgsConstructor // Cria o construtor com tudo
public class Clan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do clã é obrigatório")
    @Size(min = 3, max = 50, message = "O nome deve ter entre 3 e 50 caracteres")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "A descrição é obrigatória")
    @Column(length = 500)
    private String description;

    @NotNull(message = "A quantidade de troféus é obrigatória")
    @Min(value = 0, message = "Os troféus necessários não podem ser negativos")
    @Column(nullable = false)
    private Integer requiredTrophies;

    @JsonIgnore
    @OneToMany(mappedBy = "clan")
    private List<Player> players;
}