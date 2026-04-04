package com.allanhenrique.clashapi.repositories;

import com.allanhenrique.clashapi.entities.Village;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VillageRepository extends JpaRepository<Village, Long> {
    //busca vilas com o cv maior ou igual ao numero passado
    List<Village> findByTownHallLevelGreaterThanEqual(Integer level);
}