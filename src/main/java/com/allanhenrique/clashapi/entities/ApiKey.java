package com.allanhenrique.clashapi.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tb_api_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {

    @Id
    @Column(name = "api_key", nullable = false, unique = true)
    @Schema(description = "Chave de API gerada automaticamente", accessMode = Schema.AccessMode.READ_ONLY)
    private String key;

    @NotBlank(message = "O nome do dono é obrigatório")
    @Column(nullable = false)
    @Schema(example = "Allan", description = "Nome do dono desta chave de API")
    private String owner;

    @Column(nullable = false)
    @Schema(description = "Se a chave está ativa ou foi revogada", accessMode = Schema.AccessMode.READ_ONLY)
    private boolean active = true;

    @Column(nullable = false)
    @Schema(description = "Data e hora de criação da chave", accessMode = Schema.AccessMode.READ_ONLY)
    private Instant createdAt = Instant.now();
}