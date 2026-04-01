package com.allanhenrique.clashapi.repositories;

import com.allanhenrique.clashapi.entities.Village;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VillageRepository extends JpaRepository<Village, Long> {
}