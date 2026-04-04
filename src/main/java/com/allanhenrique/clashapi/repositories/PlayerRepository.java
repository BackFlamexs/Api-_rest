package com.allanhenrique.clashapi.repositories;

import com.allanhenrique.clashapi.entities.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    // filtro para procurar players, com esse filtro nao preciso colocar o nome exato do player. somente colocando as primeiras letras ele ja vai achar alguns nomes e posso filtrar por isso
    List<Player> findByNicknameContainingIgnoreCase(String nickname);
}