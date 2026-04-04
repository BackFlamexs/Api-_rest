package com.allanhenrique.clashapi.controllers;

import com.allanhenrique.clashapi.entities.Spell;
import com.allanhenrique.clashapi.repositories.SpellRepository;
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
@RequestMapping(value = "/spells")
@Tag(name = "Feitiços", description = "Endpoints para gerenciar os feitiços")
public class SpellController {

    @Autowired
    private SpellRepository spellRepository;

    // listando todos os proderes ja criados
    @Operation(summary = "Listar feitiços paginados")
    @GetMapping
    public ResponseEntity<Page<Spell>> findAll(Pageable pageable) {
        Page<Spell> page = spellRepository.findAll(pageable);
        return ResponseEntity.ok().body(page);
    }

    //verificando se existe os feiticos por ID
    @Operation(summary = "Buscar feitiço por ID")
    @GetMapping(value = "/{id}")
    public ResponseEntity<EntityModel<Spell>> findById(@PathVariable Long id) {
        Optional<Spell> obj = spellRepository.findById(id);
        if (obj.isEmpty()) return ResponseEntity.notFound().build();

        Spell spell = obj.get();

        Link selfLink = linkTo(methodOn(SpellController.class).findById(id)).withSelfRel();
        Link allSpellsLink = linkTo(methodOn(SpellController.class).findAll(null)).withRel("todos_feiticos");
        Link deleteLink = linkTo(methodOn(SpellController.class).delete(id)).withRel("deletar_feitico");

        return ResponseEntity.ok().body(EntityModel.of(spell, selfLink, allSpellsLink, deleteLink));
    }

    //criando um novo feitico
    @Operation(summary = "Criar novo feitiço")
    @PostMapping
    public ResponseEntity<Spell> insert(@RequestBody Spell spell) {
        Spell savedSpell = spellRepository.save(spell);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSpell);
    }

    // atualiza os dados ja existentes do feitico
    @Operation(summary = "Atualizar feitiço")
    @PutMapping(value = "/{id}")
    public ResponseEntity<Spell> update(@PathVariable Long id, @RequestBody Spell spellDetails) {
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
    @GetMapping(value = "/search")
    public ResponseEntity<List<Spell>> searchByType(@RequestParam String type) {
        List<Spell> list = spellRepository.findByTypeIgnoreCase(type);
        return ResponseEntity.ok().body(list);
    }
}