package com.allanhenrique.clashapi.repositories;

import com.allanhenrique.clashapi.entities.Troop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TroopRepository extends JpaRepository<Troop, Long> {
}