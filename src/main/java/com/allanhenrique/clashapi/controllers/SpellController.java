package com.allanhenrique.clashapi.controllers;

import com.allanhenrique.clashapi.entities.Spell;
import com.allanhenrique.clashapi.repositories.SpellRepository;
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
@RequestMapping(value = "/spells")
@Tag(name = "5. Feitiços", description = "Arsenal mágico disponível para as batalhas. Assim como as Tropas, os Feitiços funcionam como um catálogo independente. Aqui você registra os nomes das magias e classifica os seus tipos (ex: feitiços de SUPORTE, DANO, MAGIA ou AGUA). ")
public class SpellController {

    @Autowired
    private SpellRepository spellRepository;

    // listando todos os proderes ja criados
    @Operation(summary = "Listar feitiços paginados")
    @ApiResponse(responseCode = "200", description = "Lista de feiticos retornada com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos")
    @GetMapping
    public ResponseEntity<Page<Spell>> findAll(@ParameterObject @PageableDefault(page = 0, size = 10) Pageable pageable)  {
        Page<Spell> page = spellRepository.findAll(pageable);
        return ResponseEntity.ok().body(page);
    }

    //verificando se existe os feiticos por ID
    @Operation(summary = "Buscar feitiço por ID")
    @ApiResponse(responseCode = "200", description = "Registro de feitico encontrado")
    @ApiResponse(responseCode = "400", description = "ID fornecido em formato inválido")
    @ApiResponse(responseCode = "404", description = "Registro não encontrado no banco")
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

    //criando um novo feitico
    @Operation(summary = "Criar novo feitiço")
    @ApiResponse(responseCode = "201", description = "Criado um novo feitico com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos enviados")
    @PostMapping
    public ResponseEntity<Spell> insert(@Valid @RequestBody Spell spell) {
        Spell savedSpell = spellRepository.save(spell);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSpell);
    }

    // atualiza os dados ja existentes do feitico
    @Operation(summary = "Atualizar feitiço")
    @ApiResponse(responseCode = "200", description = "Atualizado o feitico com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    @ApiResponse(responseCode = "404", description = "Registro não encontrado para atualização")
    @PutMapping(value = "/{id}")
    public ResponseEntity<Spell> update(@PathVariable Long id,@Valid @RequestBody Spell spellDetails) {
        Optional<Spell> obj = spellRepository.findById(id);
        if (obj.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Spell spellToUpdate = obj.get();
        spellToUpdate.setName(spellDetails.getName());
        spellToUpdate.setType(spellDetails.getType());

        Spell updatedSpell = spellRepository.save(spellToUpdate);
        return ResponseEntity.ok().body(updatedSpell);
    }

    //deletando um feitico ja criado
    @Operation(summary = "Deletar feitiço")
    @ApiResponse(responseCode = "204", description = "Excluído o feitico com sucesso")
    @ApiResponse(responseCode = "400", description = "ID inválido")
    @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!spellRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        spellRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    //consulta por categoria, dano ou cura
    @Operation(summary = "Buscar feitiços por tipo")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros de busca inválidos")
    @GetMapping(value = "/search")
    public ResponseEntity<List<Spell>> searchByType(@RequestParam String type) {
        List<Spell> list = spellRepository.findByTypeIgnoreCase(type);
        return ResponseEntity.ok().body(list);
    }
}