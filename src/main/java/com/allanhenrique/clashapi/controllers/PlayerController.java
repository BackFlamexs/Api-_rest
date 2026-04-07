package com.allanhenrique.clashapi.controllers;

import com.allanhenrique.clashapi.entities.Player;
import com.allanhenrique.clashapi.repositories.PlayerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso de jogadores")
    @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos")
    @GetMapping
    public ResponseEntity<Page<Player>> findAll(Pageable pageable) {
        Page<Player> page = playerRepository.findAll(pageable);
        return ResponseEntity.ok().body(page);
    }
    @Operation(summary = "Buscar jogador por ID")
    @ApiResponse(responseCode = "200", description = "Registro de jogador encontrado")
    @ApiResponse(responseCode = "400", description = "ID fornecido em formato inválido")
    @ApiResponse(responseCode = "404", description = "Registro não encontrado no banco")
    @GetMapping(value = "/{id}")
    public ResponseEntity<EntityModel<Player>> findById(@PathVariable Long id) {
        Optional<Player> obj = playerRepository.findById(id);
        if (obj.isEmpty()) return ResponseEntity.notFound().build();

        Player player = obj.get();
        Link selfLink = linkTo(PlayerController.class).slash(player.getId()).withSelfRel();
        Link allPlayersLink = linkTo(PlayerController.class).withRel("todos_jogadores");
        Link deleteLink = linkTo(PlayerController.class).slash(player.getId()).withRel("deletar_jogador");

        return ResponseEntity.ok().body(EntityModel.of(player, selfLink, allPlayersLink, deleteLink));
    }

    //criando um novo player
    @Operation(summary = "Criar novo jogador")
    @ApiResponse(responseCode = "201", description = "Criado um player com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos enviados")
    @PostMapping
    public ResponseEntity<?> insert(@Valid @RequestBody Player player) {
        try {
            System.out.println("Tentando salvar o jogador: " + player.getNickname());

            Player savedPlayer = playerRepository.save(player);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedPlayer);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }
    //atualizando um player que ja existe criado e somente mudando os dados
    @Operation(summary = "Atualizar jogador")
    @ApiResponse(responseCode = "200", description = "Atualizado o player com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    @ApiResponse(responseCode = "404", description = "Registro não encontrado para atualização")
    @PutMapping(value = "/{id}")
    public ResponseEntity<Player> update(@PathVariable Long id,@Valid @RequestBody Player playerDetails) {
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
    @ApiResponse(responseCode = "204", description = "Excluído com sucesso")
    @ApiResponse(responseCode = "400", description = "ID inválido")
    @ApiResponse(responseCode = "404", description = "Registro não encontrado")
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
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros de busca inválidos")
    @GetMapping(value = "/search")
    public ResponseEntity<List<Player>> searchByNickname(@RequestParam String nickname) {
        // Aqui chamamos o método que você acabou de criar no Repository!
        List<Player> list = playerRepository.findByNicknameContainingIgnoreCase(nickname);
        return ResponseEntity.ok().body(list);
    }
}