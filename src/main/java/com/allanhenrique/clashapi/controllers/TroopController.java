package com.allanhenrique.clashapi.controllers;

import com.allanhenrique.clashapi.entities.IdempotencyKey;
import com.allanhenrique.clashapi.entities.Troop;
import com.allanhenrique.clashapi.repositories.IdempotencyKeyRepository;
import com.allanhenrique.clashapi.repositories.TroopRepository;
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
@RequestMapping(value = "/troops")
@Tag(name = "4. Tropas", description = "Catálogo de unidades do exército do jogo. Esta é uma entidade independente onde você cadastra os guerreiros de combate (como P.E.K.K.A, Dragão ou Arqueira) e define o poder de dano de cada um. Por ser um catálogo livre.")
public class TroopController {

    @Autowired
    private TroopRepository troopRepository;

    @Autowired
    private IdempotencyKeyRepository idempotencyRepository;

    @Operation(
            summary = "Listar todas as tropas",
            description = "Retorna uma lista paginada com todas as tropas cadastradas no catálogo do jogo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida")
    })
    @GetMapping
    public ResponseEntity<Page<Troop>> findAll(
            @ParameterObject @PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<Troop> page = troopRepository.findAll(pageable);
        return ResponseEntity.ok().body(page);
    }

    @Operation(
            summary = "Buscar tropa por ID",
            description = "Retorna os dados completos de uma tropa específica, incluindo seu nome e poder de dano. A resposta inclui links HATEOAS para navegação."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tropa encontrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID informado não é um número válido"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
            @ApiResponse(responseCode = "404", description = "Nenhuma tropa encontrada com este ID")
    })
    @GetMapping(value = "/{id}")
    public ResponseEntity<EntityModel<Troop>> findById(@PathVariable Long id) {
        Optional<Troop> obj = troopRepository.findById(id);
        if (obj.isEmpty()) return ResponseEntity.notFound().build();

        Troop troop = obj.get();
        Link selfLink = linkTo(methodOn(TroopController.class).findById(id)).withSelfRel();
        Link allTroopsLink = linkTo(TroopController.class).withRel("todas_tropas");
        Link deleteLink = linkTo(methodOn(TroopController.class).delete(id)).withRel("deletar_tropa");

        return ResponseEntity.ok().body(EntityModel.of(troop, selfLink, allTroopsLink, deleteLink));
    }

    @Operation(
            summary = "Cadastrar nova tropa",
            description = "Adiciona uma nova tropa ao catálogo do jogo. Use o header X-Idempotency-Key para evitar cadastros duplicados em caso de falha de rede."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tropa cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Algum campo obrigatório está faltando ou com valor inválido"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
            @ApiResponse(responseCode = "409", description = "Requisição duplicada bloqueada pela chave de idempotência")
    })
    @PostMapping
    public ResponseEntity<Troop> insert(
            @io.swagger.v3.oas.annotations.Parameter(name = "X-Idempotency-Key", description = "Chave única para evitar cadastros duplicados em caso de retentativa", in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER)
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody Troop troop) {

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            if (idempotencyRepository.existsById(idempotencyKey)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta operação já foi processada anteriormente.");
            }
        }

        try {
            Troop savedTroop = troopRepository.saveAndFlush(troop);
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                idempotencyRepository.save(new IdempotencyKey(idempotencyKey));
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTroop);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Conflito de dados: tropa já cadastrada.");
        }
    }

    @Operation(
            summary = "Atualizar tropa",
            description = "Atualiza o nome e o valor de dano de uma tropa existente no catálogo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tropa atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados enviados contêm valores inválidos"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
            @ApiResponse(responseCode = "404", description = "Nenhuma tropa encontrada com este ID")
    })
    @PutMapping(value = "/{id}")
    public ResponseEntity<Troop> update(@PathVariable Long id, @Valid @RequestBody Troop troopDetails) {
        Troop troopToUpdate = troopRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tropa não encontrada."));

        troopToUpdate.setName(troopDetails.getName());
        troopToUpdate.setDamage(troopDetails.getDamage());
        troopToUpdate.setDamageType(troopDetails.getDamageType());

        return ResponseEntity.ok().body(troopRepository.save(troopToUpdate));
    }

    @Operation(
            summary = "Remover tropa",
            description = "Remove permanentemente uma tropa do catálogo. Jogadores que possuíam esta tropa perderão o vínculo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tropa removida com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID informado não é um número válido"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
            @ApiResponse(responseCode = "404", description = "Nenhuma tropa encontrada com este ID")
    })
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!troopRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        troopRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Filtrar tropas por dano mínimo",
            description = "Retorna todas as tropas cujo dano seja maior ou igual ao valor informado. Útil para encontrar as unidades mais poderosas do catálogo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro de dano inválido"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida")
    })
    @GetMapping(value = "/search")
    public ResponseEntity<List<Troop>> searchByDamage(
            @RequestParam(name = "minDamage", defaultValue = "0") Integer minDamage) {
        List<Troop> list = troopRepository.findByDamageGreaterThanEqual(minDamage);
        return ResponseEntity.ok().body(list);
    }
}