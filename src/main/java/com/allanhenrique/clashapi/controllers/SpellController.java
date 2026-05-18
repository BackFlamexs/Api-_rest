package com.allanhenrique.clashapi.controllers;

import com.allanhenrique.clashapi.entities.IdempotencyKey;
import com.allanhenrique.clashapi.entities.Spell;
import com.allanhenrique.clashapi.repositories.IdempotencyKeyRepository;
import com.allanhenrique.clashapi.repositories.SpellRepository;
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
@RequestMapping(value = "/spells")
@Tag(name = "5. Feitiços", description = "Arsenal mágico disponível para as batalhas. Assim como as Tropas, os Feitiços funcionam como um catálogo independente. Aqui você registra os nomes das magias e classifica os seus tipos (ex: feitiços de SUPORTE, DANO, MAGIA ou AGUA).")
public class SpellController {

    @Autowired
    private SpellRepository spellRepository;

    @Autowired
    private IdempotencyKeyRepository idempotencyRepository;

    @Operation(
            summary = "Listar todos os feitiços",
            description = "Retorna uma lista paginada com todos os feitiços cadastrados no catálogo do jogo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida")
    })
    @GetMapping
    public ResponseEntity<Page<Spell>> findAll(
            @ParameterObject @PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<Spell> page = spellRepository.findAll(pageable);
        return ResponseEntity.ok().body(page);
    }

    @Operation(
            summary = "Buscar feitiço por ID",
            description = "Retorna os dados completos de um feitiço específico, incluindo nome e tipo. A resposta inclui links HATEOAS para navegação."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feitiço encontrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID informado não é um número válido"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
            @ApiResponse(responseCode = "404", description = "Nenhum feitiço encontrado com este ID")
    })
    @GetMapping(value = "/{id}")
    public ResponseEntity<EntityModel<Spell>> findById(@PathVariable Long id) {
        Optional<Spell> obj = spellRepository.findById(id);
        if (obj.isEmpty()) return ResponseEntity.notFound().build();

        Spell spell = obj.get();
        Link selfLink = linkTo(methodOn(SpellController.class).findById(id)).withSelfRel();
        Link allSpellsLink = linkTo(SpellController.class).withRel("todos_feiticos");
        Link deleteLink = linkTo(methodOn(SpellController.class).delete(id)).withRel("deletar_feitico");

        return ResponseEntity.ok().body(EntityModel.of(spell, selfLink, allSpellsLink, deleteLink));
    }

    @Operation(
            summary = "Cadastrar novo feitiço",
            description = "Adiciona um novo feitiço ao catálogo do jogo. Use o header X-Idempotency-Key para evitar cadastros duplicados em caso de falha de rede."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Feitiço cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Algum campo obrigatório está faltando ou com valor inválido"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
            @ApiResponse(responseCode = "409", description = "Requisição duplicada bloqueada pela chave de idempotência")
    })
    @PostMapping
    public ResponseEntity<Spell> insert(
            @io.swagger.v3.oas.annotations.Parameter(name = "X-Idempotency-Key", description = "Chave única para evitar cadastros duplicados em caso de retentativa", in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER)
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody Spell spell) {

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            if (idempotencyRepository.existsById(idempotencyKey)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta operação já foi processada anteriormente.");
            }
        }

        try {
            Spell savedSpell = spellRepository.saveAndFlush(spell);
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                idempotencyRepository.save(new IdempotencyKey(idempotencyKey));
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(savedSpell);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Conflito de dados: feitiço já cadastrado.");
        }
    }

    @Operation(
            summary = "Atualizar feitiço",
            description = "Atualiza o nome e o tipo de um feitiço existente no catálogo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feitiço atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados enviados contêm valores inválidos"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
            @ApiResponse(responseCode = "404", description = "Nenhum feitiço encontrado com este ID")
    })
    @PutMapping(value = "/{id}")
    public ResponseEntity<Spell> update(@PathVariable Long id, @Valid @RequestBody Spell spellDetails) {
        Spell spellToUpdate = spellRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feitiço não encontrado."));

        spellToUpdate.setName(spellDetails.getName());
        spellToUpdate.setType(spellDetails.getType());

        return ResponseEntity.ok().body(spellRepository.save(spellToUpdate));
    }

    @Operation(
            summary = "Remover feitiço",
            description = "Remove permanentemente um feitiço do catálogo. Jogadores que possuíam este feitiço perderão o vínculo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Feitiço removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID informado não é um número válido"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida"),
            @ApiResponse(responseCode = "404", description = "Nenhum feitiço encontrado com este ID")
    })
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!spellRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        spellRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Filtrar feitiços por tipo",
            description = "Retorna todos os feitiços de um determinado tipo. Exemplos de tipos válidos: DANO, SUPORTE, CURA, SALTO. A busca não diferencia maiúsculas de minúsculas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro de tipo inválido"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou inválida")
    })
    @GetMapping(value = "/search")
    public ResponseEntity<List<Spell>> searchByType(@RequestParam String type) {
        List<Spell> list = spellRepository.findByTypeIgnoreCase(type);
        return ResponseEntity.ok().body(list);
    }
}