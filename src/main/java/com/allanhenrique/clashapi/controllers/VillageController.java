package com.allanhenrique.clashapi.controllers;

import com.allanhenrique.clashapi.entities.IdempotencyKey;
import com.allanhenrique.clashapi.entities.Player;
import com.allanhenrique.clashapi.entities.Village;
import com.allanhenrique.clashapi.repositories.IdempotencyKeyRepository;
import com.allanhenrique.clashapi.repositories.PlayerRepository;
import com.allanhenrique.clashapi.repositories.VillageRepository;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

@RestController
@RequestMapping(value = "/villages")
@Tag(name = "3. Vilas", description = "Controle das bases físicas dos jogadores. A Vila tem um relacionamento direto e exclusivo (1 para 1) com um Jogador. Você pode enviar os dados do Jogador junto, e o sistema criará ambos simultaneamente. Da mesma forma, deletar uma Vila irá remover o jogador dono dela, garantindo a integridade dos dados e evitando registros órfãos.")
public class VillageController {

    @Autowired
    private VillageRepository villageRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private IdempotencyKeyRepository idempotencyRepository;

    @Operation(
            summary = "Listar todas as vilas",
            description = "Retorna uma lista paginada com todas as vilas cadastradas. Cada vila está sempre vinculada a um jogador."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida")
    })
    @GetMapping
    public ResponseEntity<Page<Village>> findAll(
            @ParameterObject @PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<Village> page = villageRepository.findAll(pageable);
        return ResponseEntity.ok().body(page);
    }

    @Operation(
            summary = "Buscar vila por ID",
            description = "Retorna os dados de uma vila específica junto com as informações do jogador dono. A resposta inclui links HATEOAS para navegação."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vila encontrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID informado não é um número válido"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
            @ApiResponse(responseCode = "404", description = "Nenhuma vila encontrada com este ID")
    })
    @GetMapping(value = "/{id}")
    public ResponseEntity<EntityModel<Village>> findById(@PathVariable Long id) {
        Village village = villageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vila não encontrada"));

        Link selfLink = linkTo(methodOn(VillageController.class).findById(id)).withSelfRel();
        Link allVillagesLink = linkTo(VillageController.class).withRel("todas_vilas");
        Link deleteLink = linkTo(methodOn(VillageController.class).delete(id)).withRel("deletar_vila");

        return ResponseEntity.ok().body(EntityModel.of(village, selfLink, allVillagesLink, deleteLink));
    }

    @Operation(
            summary = "Criar nova vila",
            description = "Cria uma vila e a vincula a um jogador existente. Cada jogador só pode ter uma vila — tentativas de criar uma segunda retornam erro 400. Use o header X-Idempotency-Key para evitar criações duplicadas em caso de falha de rede."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vila criada e vinculada ao jogador com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou jogador já possui uma vila cadastrada"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
            @ApiResponse(responseCode = "404", description = "O jogador informado não existe no sistema"),
            @ApiResponse(responseCode = "409", description = "Requisição duplicada bloqueada pela chave de idempotência")
    })
    @PostMapping
    public ResponseEntity<Village> insert(
            @io.swagger.v3.oas.annotations.Parameter(name = "X-Idempotency-Key", description = "Chave única para evitar criações duplicadas em caso de retentativa", in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER)
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody Village village) {

        if (village.getPlayer() == null || village.getPlayer().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID do jogador é obrigatório.");
        }

        Player player = playerRepository.findById(village.getPlayer().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jogador não encontrado"));

        if (villageRepository.existsByPlayerId(player.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este jogador já possui uma vila cadastrada.");
        }

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            if (idempotencyRepository.existsById(idempotencyKey)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta operação já foi processada anteriormente.");
            }
        }

        village.setPlayer(player);
        try {
            Village savedVillage = villageRepository.saveAndFlush(village);
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                idempotencyRepository.save(new IdempotencyKey(idempotencyKey));
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(savedVillage);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Conflito de dados: este jogador já possui uma vila (violação de constraint).");
        }
    }

    @Operation(
            summary = "Atualizar vila",
            description = "Atualiza o nome e o nível do Centro de Vila. O jogador dono não pode ser alterado por aqui."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vila atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados enviados contêm valores inválidos"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
            @ApiResponse(responseCode = "404", description = "Nenhuma vila encontrada com este ID")
    })
    @PutMapping(value = "/{id}")
    public ResponseEntity<Village> update(@PathVariable Long id, @Valid @RequestBody Village villageDetails) {
        Village villageToUpdate = villageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vila não encontrada"));

        villageToUpdate.setName(villageDetails.getName());
        villageToUpdate.setTownHallLevel(villageDetails.getTownHallLevel());

        return ResponseEntity.ok().body(villageRepository.save(villageToUpdate));
    }

    @Operation(
            summary = "Remover vila",
            description = "Remove permanentemente uma vila do sistema. O jogador vinculado a ela não é removido — apenas o vínculo é desfeito."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vila removida com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID informado não é um número válido"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
            @ApiResponse(responseCode = "404", description = "Nenhuma vila encontrada com este ID")
    })
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!villageRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vila não encontrada");
        }
        villageRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Filtrar vilas por nível do Centro de Vila",
            description = "Retorna todas as vilas cujo nível do Centro de Vila seja maior ou igual ao valor informado. Útil para encontrar as bases mais avançadas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro de nível inválido"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida")
    })
    @GetMapping(value = "/search")
    public ResponseEntity<List<Village>> searchByTownHallLevel(
            @RequestParam(name = "minLevel", defaultValue = "1") Integer minLevel) {
        List<Village> list = villageRepository.findByTownHallLevelGreaterThanEqual(minLevel);
        return ResponseEntity.ok().body(list);
    }
}