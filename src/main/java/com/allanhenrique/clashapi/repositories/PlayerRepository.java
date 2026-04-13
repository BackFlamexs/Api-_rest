package com.allanhenrique.clashapi.repositories;

import com.allanhenrique.clashapi.entities.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    // filtro para procurar players, com esse filtro nao preciso colocar o nome exato do player. somente colocando as primeiras letras ele ja vai achar alguns nomes e posso filtrar por isso
    List<Player> findByNicknameContainingIgnoreCase(String nickname);
    List<Player> findByClanId(Long clanId);

    @Modifying
    @Transactional
    @Query("UPDATE Player p SET p.clan = null WHERE p.clan.id = :clanId")
    void removeClanFromAllPlayers(@Param("clanId") Long clanId);
}