package com.allanhenrique.clashapi.controllers;

import com.allanhenrique.clashapi.dto.ClanV2Response;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;

@RestController
@RequestMapping(value = "/clans")
@Tag(name = "1. Clãs", description = "Gerenciamento central das alianças do jogo. Aqui você pode criar novos clãs, definir a quantidade mínima de troféus exigida para entrada e atualizar as informações do grupo. O Clã é a entidade raiz do sistema.")
public class ClanController {

    @Autowired
    private ClanRepository clanRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private IdempotencyKeyRepository idempotencyRepository;

    @Operation(
            summary = "Lista todos os clãs",
            description = "Retorna uma lista paginada de clãs. " +
                    "Suporta versionamento via header X-API-Version: " +
                    "versão 1 retorna o formato padrão com links HATEOAS, " +
                    "versão 2 retorna o formato estendido incluindo o total de membros de cada clã."
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos")
    @GetMapping
    public ResponseEntity<?> findAll(
            @ParameterObject @PageableDefault(page = 0, size = 10) Pageable pageable,
            @io.swagger.v3.oas.annotations.Parameter(name = "X-API-Version", description = "Versão da API (1 ou 2)", in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER)
            @RequestHeader(value = "X-API-Version", required = false, defaultValue = "1") String apiVersion) {

        Page<Clan> clans = clanRepository.findAll(pageable);

        // V2: retorna com campo totalMembers
        if ("2".equals(apiVersion)) {
            List<ClanV2Response> v2List = clans.stream().map(clan -> {
                int totalMembers = playerRepository.findByClanId(clan.getId()).size();
                return new ClanV2Response(
                        clan.getId(),
                        clan.getName(),
                        clan.getDescription(),
                        clan.getRequiredTrophies(),
                        totalMembers
                );
            }).collect(Collectors.toList());
            return ResponseEntity.ok()
                    .header("X-API-Version", "2")
                    .body(v2List);
        }

        // V1 (padrão): retorna com HATEOAS
        List<EntityModel<Clan>> clanModels = clans.stream()
                .map(clan -> EntityModel.of(clan,
                        linkTo(methodOn(ClanController.class).findById(clan.getId())).withSelfRel()))
                .toList();
        Link selfLink = linkTo(methodOn(ClanController.class).findAll(pageable, apiVersion)).withSelfRel();
        return ResponseEntity.ok()
                .header("X-API-Version", "1")
                .body(CollectionModel.of(clanModels, selfLink));
    }

    @Operation(summary = "Busca clã por ID")
    @ApiResponse(responseCode = "200", description = "Registro encontrado")
    @ApiResponse(responseCode = "400", description = "ID fornecido em formato inválido")
    @ApiResponse(responseCode = "404", description = "Registro não encontrado no banco")
    @GetMapping(value = "/{id}")
    public ResponseEntity<EntityModel<Clan>> findById(@PathVariable Long id) {
        Optional<Clan> obj = clanRepository.findById(id);
        if (obj.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Clan clan = obj.get();
        Link selfLink = linkTo(methodOn(ClanController.class).findById(id)).withSelfRel();
        Link allClansLink = linkTo(ClanController.class).withRel("todos_clans");
        Link deleteLink = linkTo(methodOn(ClanController.class).delete(id)).withRel("deletar_este_clã");
        return ResponseEntity.ok().body(EntityModel.of(clan, selfLink, allClansLink, deleteLink));
    }

    @Operation(summary = "Criar novo clã")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Clã criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos enviados"),
            @ApiResponse(responseCode = "409", description = "Conflito: operação já processada (Idempotência)")
    })
    @PostMapping
    public ResponseEntity<Clan> insert(
            @io.swagger.v3.oas.annotations.Parameter(name = "X-Idempotency-Key", description = "Chave para evitar duplicidade", in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER)
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody Clan clan) {

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            if (idempotencyRepository.existsById(idempotencyKey)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta operação já foi processada anteriormente.");
            }
        }

        try {
            Clan savedClan = clanRepository.saveAndFlush(clan);
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                idempotencyRepository.save(new IdempotencyKey(idempotencyKey));
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(savedClan);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Conflito de dados: já existe um clã com esse nome.");
        }
    }

    @Operation(summary = "Atualizar um clã que já foi criado")
    @ApiResponse(responseCode = "200", description = "Clã atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    @ApiResponse(responseCode = "404", description = "Registro não encontrado para atualização")
    @PutMapping(value = "/{id}")
    public ResponseEntity<Clan> update(@PathVariable Long id, @Valid @RequestBody Clan clanDetails) {
        Optional<Clan> obj = clanRepository.findById(id);
        if (obj.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Clan clanToUpdate = obj.get();
        clanToUpdate.setName(clanDetails.getName());
        clanToUpdate.setDescription(clanDetails.getDescription());
        clanToUpdate.setRequiredTrophies(clanDetails.getRequiredTrophies());
        return ResponseEntity.ok().body(clanRepository.save(clanToUpdate));
    }

    @Operation(summary = "Deletar clã")
    @ApiResponse(responseCode = "204", description = "Clã excluído com sucesso")
    @ApiResponse(responseCode = "400", description = "ID do clã inválido")
    @ApiResponse(responseCode = "404", description = "Registro do clã não encontrado")
    @Transactional
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!clanRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<Player> players = playerRepository.findByClanId(id);
        for (Player player : players) {
            player.setClan(null);
            playerRepository.save(player);
        }
        clanRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Filtrar clãs por troféus mínimos")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros de busca inválidos")
    @GetMapping(value = "/search")
    public ResponseEntity<List<Clan>> searchByTrophies(
            @RequestParam(name = "minTrophies", defaultValue = "0") Integer minTrophies) {
        List<Clan> list = clanRepository.findByRequiredTrophiesGreaterThanEqual(minTrophies);
        return ResponseEntity.ok().body(list);
    }
}