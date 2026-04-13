package com.allanhenrique.clashapi.controllers;

import com.allanhenrique.clashapi.entities.Clan;
import com.allanhenrique.clashapi.entities.Player;
import com.allanhenrique.clashapi.repositories.ClanRepository;
import com.allanhenrique.clashapi.repositories.PlayerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@Tag(name = "2. Jogadores", description = "Administração de todos os usuários (Players) do ecossistema. Este módulo permite o cadastro de novos jogadores, definição de seus níveis de experiência e alocação de cargos específicos dentro do grupo (como LIDER, CO_LIDER, ANCIAO e MEMBRO).")
public class PlayerController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ClanRepository clanRepository;

    @Operation(summary = "Listar jogadores paginados")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso de jogadores")
    @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos")
    @GetMapping
    public ResponseEntity<Page<Player>> findAll(@ParameterObject @PageableDefault(page = 0, size = 10) Pageable pageable)  {
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

            // --- INÍCIO DO CÓDIGO NOVO ---
            if (player.getClan() != null && player.getClan().getId() != null) {
                java.util.Optional<Clan> clanBuscado = clanRepository.findById(player.getClan().getId());

                if (clanBuscado.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro: Clã com ID " + player.getClan().getId() + " não encontrado.");
                }

                player.setClan(clanBuscado.get());
            }

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
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Player playerDetails) {
        System.out.println("🔥 [DEBUG] Entrou no PUT! Tentando atualizar o jogador ID: " + id);

        Optional<Player> obj = playerRepository.findById(id);
        if (obj.isEmpty()) {
            System.out.println("❌ [DEBUG] Falhou: Jogador ID " + id + " não encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro: Jogador com ID " + id + " não encontrado.");
        }

        Player playerToUpdate = obj.get();
        System.out.println("✅ [DEBUG] Jogador encontrado: " + playerToUpdate.getNickname());

        playerToUpdate.setNickname(playerDetails.getNickname());
        playerToUpdate.setLevel(playerDetails.getLevel());
        playerToUpdate.setRole(playerDetails.getRole());

        // Verificando o clã
        if (playerDetails.getClan() != null && playerDetails.getClan().getId() != null) {
            Long clanId = playerDetails.getClan().getId();
            System.out.println("🔍 [DEBUG] Buscando Clã novo com ID: " + clanId);

            Optional<Clan> clanBuscado = clanRepository.findById(clanId);

            if (clanBuscado.isEmpty()) {
                System.out.println("❌ [DEBUG] Falhou: Clã ID " + clanId + " não existe!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro: Clã com ID " + clanId + " não existe no banco.");
            }

            System.out.println("✅ [DEBUG] Clã encontrado! Vinculando...");
            playerToUpdate.setClan(clanBuscado.get());
        } else {
            System.out.println("⚠️ [DEBUG] JSON veio sem clã. Removendo clã do jogador...");
            playerToUpdate.setClan(null);
        }

        Player updatedPlayer = playerRepository.save(playerToUpdate);
        System.out.println("🚀 [DEBUG] Sucesso! Jogador atualizado no banco.");

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
        List<Player> list = playerRepository.findByNicknameContainingIgnoreCase(nickname);
        return ResponseEntity.ok().body(list);
    }
}