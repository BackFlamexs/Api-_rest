package com.allanhenrique.clashapi.repositories;

import com.allanhenrique.clashapi.entities.Clan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClanRepository extends JpaRepository<Clan, Long> {

    Page<Clan> findByNameContainingIgnoreCase(String name, Pageable pageable);
}