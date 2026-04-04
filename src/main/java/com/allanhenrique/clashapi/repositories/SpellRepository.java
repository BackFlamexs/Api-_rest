package com.allanhenrique.clashapi.repositories;

import com.allanhenrique.clashapi.entities.Spell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpellRepository extends JpaRepository<Spell, Long> {
    // busca por feiticos pela categoria, tipo cura ou dano
    List<Spell> findByTypeIgnoreCase(String type);
}