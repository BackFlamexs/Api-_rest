package com.allanhenrique.clashapi.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_idempotency_keys")
public class IdempotencyKey {

    @Id
    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String key;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Construtor padrão (JPA)
    public IdempotencyKey() {}

    // Construtor auxiliar
    public IdempotencyKey(String key) {
        this.key = key;
        this.createdAt = LocalDateTime.now();
    }

    // Getters e Setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}