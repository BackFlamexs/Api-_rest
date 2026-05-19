package com.allanhenrique.clashapi.controllers;

import com.allanhenrique.clashapi.entities.Clan;
import com.allanhenrique.clashapi.entities.IdempotencyKey;
import com.allanhenrique.clashapi.entities.Player;
import com.allanhenrique.clashapi.repositories.ClanRepository;
import com.allanhenrique.clashapi.repositories.IdempotencyKeyRepository;
import com.allanhenrique.clashapi.repositories.PlayerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @Autowired
    private IdempotencyKeyRepository idempotencyRepository;

    @Operation(
            summary = "Listar todos os jogadores",
            description = "Retorna uma lista paginada com todos os jogadores cadastrados no sistema. Use os parâmetros de paginação para controlar quantos registros receber por vez."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida")
    })
    @GetMapping
    public ResponseEntity<List<Player>> findAll() {
        List<Player> list = playerRepository.findAll();
        return ResponseEntity.ok().body(list);
    }

    @Operation(
            summary = "Buscar jogador por ID",
            description = "Retorna os dados completos de um jogador específico, incluindo seu clã e cargo atual. A resposta inclui links HATEOAS para navegação."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jogador encontrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID informado não é um número válido"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
            @ApiResponse(responseCode = "404", description = "Nenhum jogador encontrado com este ID")
    })
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

    @Operation(
            summary = "Cadastrar novo jogador",
            description = "Cria um novo jogador no sistema. O clã é opcional — se informado, o jogador já entra vinculado a ele. Use o header X-Idempotency-Key para evitar cadastros duplicados em caso de falha de rede."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Jogador cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Algum campo obrigatório está faltando ou com valor inválido"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
            @ApiResponse(responseCode = "404", description = "O clã informado não existe no sistema"),
            @ApiResponse(responseCode = "409", description = "Requisição duplicada bloqueada pela chave de idempotência")
    })
    @PostMapping
    public ResponseEntity<Player> insert(
            @io.swagger.v3.oas.annotations.Parameter(name = "X-Idempotency-Key", description = "Chave única para evitar cadastros duplicados em caso de retentativa", in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER)
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody Player player) {

        if (player.getClan() != null && player.getClan().getId() != null) {
            Clan clan = clanRepository.findById(player.getClan().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Clã com ID " + player.getClan().getId() + " não encontrado."));
            player.setClan(clan);
        }

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            if (idempotencyRepository.existsById(idempotencyKey)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta operação já foi processada anteriormente.");
            }
        }

        try {
            Player savedPlayer = playerRepository.saveAndFlush(player);
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                idempotencyRepository.save(new IdempotencyKey(idempotencyKey));
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPlayer);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Conflito de dados: nickname já cadastrado.");
        }
    }

    @Operation(
            summary = "Atualizar dados do jogador",
            description = "Atualiza o nickname, nível, cargo e clã de um jogador existente. Para remover o jogador de um clã, envie o campo clan como nulo ou omita ele no body."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados atualizados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados enviados contêm valores inválidos"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
            @ApiResponse(responseCode = "404", description = "Jogador ou clã informado não encontrado")
    })
    @PutMapping(value = "/{id}")
    public ResponseEntity<Player> update(@PathVariable Long id, @Valid @RequestBody Player playerDetails) {
        Player playerToUpdate = playerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Jogador com ID " + id + " não encontrado."));

        playerToUpdate.setNickname(playerDetails.getNickname());
        playerToUpdate.setLevel(playerDetails.getLevel());
        playerToUpdate.setRole(playerDetails.getRole());

        if (playerDetails.getClan() != null && playerDetails.getClan().getId() != null) {
            Clan clan = clanRepository.findById(playerDetails.getClan().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Clã com ID " + playerDetails.getClan().getId() + " não existe."));
            playerToUpdate.setClan(clan);
        } else {
            playerToUpdate.setClan(null);
        }

        return ResponseEntity.ok().body(playerRepository.save(playerToUpdate));
    }

    @Operation(
            summary = "Remover jogador",
            description = "Remove permanentemente um jogador do sistema. Atenção: se o jogador possuir uma vila vinculada, ela também será removida automaticamente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Jogador removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID informado não é um número válido"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
            @ApiResponse(responseCode = "404", description = "Nenhum jogador encontrado com este ID")
    })
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!playerRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        playerRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Buscar jogador por nickname",
            description = "Busca jogadores cujo nickname contenha o termo informado. A busca não diferencia letras maiúsculas de minúsculas, então 'allan' encontra 'Allan_Pro'."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro de busca inválido"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida")
    })
    @GetMapping(value = "/search")
    public ResponseEntity<List<Player>> searchByNickname(@RequestParam String nickname) {
        List<Player> list = playerRepository.findByNicknameContainingIgnoreCase(nickname);
        return ResponseEntity.ok().body(list);
    }
}