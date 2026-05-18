package com.allanhenrique.clashapi.repositories;

import com.allanhenrique.clashapi.entities.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, String> {

    Optional<ApiKey> findByKeyAndActiveTrue(String key);
}