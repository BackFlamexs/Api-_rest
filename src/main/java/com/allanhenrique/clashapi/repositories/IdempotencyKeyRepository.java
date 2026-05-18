package com.allanhenrique.clashapi.repositories;

import com.allanhenrique.clashapi.entities.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {
}