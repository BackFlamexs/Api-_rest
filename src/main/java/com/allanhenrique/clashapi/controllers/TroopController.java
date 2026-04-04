package com.allanhenrique.clashapi.controllers;

import com.allanhenrique.clashapi.entities.Troop;
import com.allanhenrique.clashapi.repositories.TroopRepository;
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
@RequestMapping(value = "/troops")
@Tag(name = "Tropas", description = "Endpoints para gerenciar as tropas")
public class TroopController {

    @Autowired
    private TroopRepository troopRepository;

    //listando paginas de tropas
    @Operation(summary = "Listar tropas paginadas")
    @GetMapping
    public ResponseEntity<Page<Troop>> findAll(Pageable pageable) {
        Page<Troop> page = troopRepository.findAll(pageable);
        return ResponseEntity.ok().body(page);
    }

    //buscando tropas por ID se nao encontrar ele retorna 404
    @Operation(summary = "Buscar tropa por ID")
    @GetMapping(value = "/{id}")
    public ResponseEntity<EntityModel<Troop>> findById(@PathVariable Long id) {
        Optional<Troop> obj = troopRepository.findById(id);
        if (obj.isEmpty()) return ResponseEntity.notFound().build();

        Troop troop = obj.get();

        Link selfLink = linkTo(methodOn(TroopController.class).findById(id)).withSelfRel();
        Link allTroopsLink = linkTo(methodOn(TroopController.class).findAll(null)).withRel("todas_tropas");
        Link deleteLink = linkTo(methodOn(TroopController.class).delete(id)).withRel("deletar_tropa");

        return ResponseEntity.ok().body(EntityModel.of(troop, selfLink, allTroopsLink, deleteLink));
    }


    //inserindo uma nova tropa, te, que retorna um 201 para criada
    @Operation(summary = "Criar nova tropa")
    @PostMapping
    public ResponseEntity<Troop> insert(@RequestBody Troop troop) {
        Troop savedTroop = troopRepository.save(troop);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTroop);
    }

    //atualizando os dados das tropas ja existentes
    @Operation(summary = "Atualizar tropa")
    @PutMapping(value = "/{id}")
    public ResponseEntity<Troop> update(@PathVariable Long id, @RequestBody Troop troopDetails) {
        Optional<Troop> obj = troopRepository.findById(id);
        if (obj.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Troop troopToUpdate = obj.get();
        troopToUpdate.setName(troopDetails.getName());
        troopToUpdate.setDamage(troopDetails.getDamage());

        Troop updatedTroop = troopRepository.save(troopToUpdate);
        return ResponseEntity.ok().body(updatedTroop);
    }

    // deletando uma tropa ja criada
    @Operation(summary = "Deletar tropa")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!troopRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        troopRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    //consulta por tropas com dano maior ou igual
    @Operation(summary = "Filtrar tropas por dano mínimo")
    @GetMapping(value = "/search")
    public ResponseEntity<List<Troop>> searchByDamage(@RequestParam(name = "minDamage", defaultValue = "0") Integer minDamage) {
        List<Troop> list = troopRepository.findByDamageGreaterThanEqual(minDamage);
        return ResponseEntity.ok().body(list);
    }
}