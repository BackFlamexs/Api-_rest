package com.allanhenrique.clashapi.repositories;

import com.allanhenrique.clashapi.entities.Troop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TroopRepository extends JpaRepository<Troop, Long> {
    //busca por tropas com o dano maior ou igual
    List<Troop> findByDamageGreaterThanEqual(Integer minDamage);
}