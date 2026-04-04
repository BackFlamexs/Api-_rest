package com.allanhenrique.clashapi.controllers;

import com.allanhenrique.clashapi.entities.Player;
import com.allanhenrique.clashapi.repositories.PlayerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

@RestController
@RequestMapping(value = "/players")
@Tag(name = "Jogadores", description = "Endpoints para gerenciar os jogadores")
public class PlayerController {

    @Autowired
    private PlayerRepository playerRepository;

    @Operation(summary = "Listar jogadores paginados")
    @GetMapping
    public ResponseEntity<Page<Player>> findAll(Pageable pageable) {
        Page<Player> page = playerRepository.findAll(pageable);
        return ResponseEntity.ok().body(page);
    }
    @Operation(summary = "Buscar jogador por ID")
    @GetMapping(value = "/{id}")
    public ResponseEntity<EntityModel<Player>> findById(@PathVariable Long id) {
        Optional<Player> obj = playerRepository.findById(id);
        if (obj.isEmpty()) return ResponseEntity.notFound().build();

        Player player = obj.get();
        Link selfLink = linkTo(methodOn(PlayerController.class).findById(id)).withSelfRel();
        Link allPlayersLink = linkTo(methodOn(PlayerController.class).findAll(null)).withRel("todos_jogadores");
        Link deleteLink = linkTo(methodOn(PlayerController.class).delete(id)).withRel("deletar_jogador");

        return ResponseEntity.ok().body(EntityModel.of(player, selfLink, allPlayersLink, deleteLink));
    }

    //criando um novo player
    @Operation(summary = "Criar novo jogador")
    @PostMapping
    public ResponseEntity<Player> insert(@RequestBody Player player) {
        Player savedPlayer = playerRepository.save(player);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPlayer);
    }
    //atualizando um player que ja existe criado e somente mudando os dados
    @Operation(summary = "Atualizar jogador")
    @PutMapping(value = "/{id}")
    public ResponseEntity<Player> update(@PathVariable Long id, @RequestBody Player playerDetails) {
        Optional<Player> obj = playerRepository.findById(id);
        if (obj.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Player playerToUpdate = obj.get();
        playerToUpdate.setNickname(playerDetails.getNickname());
        playerToUpdate.setLevel(playerDetails.getLevel());
        playerToUpdate.setRole(playerDetails.getRole());

        Player updatedPlayer = playerRepository.save(playerToUpdate);
        return ResponseEntity.ok().body(updatedPlayer);
    }
    //Excluindo um player
    @Operation(summary = "Deletar jogador")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!playerRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        playerRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    // consulta para puxar os dados de usuario que nao precisa colocar o nome inteiro dele
    @Operation(summary = "Buscar jogador por Nickname (filtro)")
    @GetMapping(value = "/search")
    public ResponseEntity<List<Player>> searchByNickname(@RequestParam String nickname) {
        // Aqui chamamos o método que você acabou de criar no Repository!
        List<Player> list = playerRepository.findByNicknameContainingIgnoreCase(nickname);
        return ResponseEntity.ok().body(list);
    }
}