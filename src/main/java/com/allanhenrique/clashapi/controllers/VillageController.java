package com.allanhenrique.clashapi.controllers;

import com.allanhenrique.clashapi.entities.Village;
import com.allanhenrique.clashapi.repositories.VillageRepository;
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
@RequestMapping(value = "/villages")
@Tag(name = "Vilas", description = "Endpoints para gerenciar as vilas dos jogadores")
public class VillageController {

    @Autowired
    private VillageRepository villageRepository;

    @Operation(summary = "Listar todas as vilas")
    @GetMapping
    public ResponseEntity<Page<Village>> findAll(Pageable pageable) {
        Page<Village> page = villageRepository.findAll(pageable);
        return ResponseEntity.ok().body(page);
    }

    @Operation(summary = "Buscar vila por ID")
    @GetMapping(value = "/{id}")
    public ResponseEntity<EntityModel<Village>> findById(@PathVariable Long id) {
        Optional<Village> obj = villageRepository.findById(id);
        if (obj.isEmpty()) return ResponseEntity.notFound().build();

        Village village = obj.get();

        Link selfLink = linkTo(methodOn(VillageController.class).findById(id)).withSelfRel();
        Link allVillagesLink = linkTo(methodOn(VillageController.class).findAll(null)).withRel("todas_vilas");
        Link deleteLink = linkTo(methodOn(VillageController.class).delete(id)).withRel("deletar_vila");

        return ResponseEntity.ok().body(EntityModel.of(village, selfLink, allVillagesLink, deleteLink));
    }

    @Operation(summary = "Criar nova vila")
    @PostMapping
    public ResponseEntity<Village> insert(@RequestBody Village village) {
        Village savedVillage = villageRepository.save(village);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedVillage);
    }

    @Operation(summary = "Atualizar vila")
    @PutMapping(value = "/{id}")
    public ResponseEntity<Village> update(@PathVariable Long id, @RequestBody Village villageDetails) {
        Optional<Village> obj = villageRepository.findById(id);
        if (obj.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Village villageToUpdate = obj.get();
        villageToUpdate.setName(villageDetails.getName());
        villageToUpdate.setTownHallLevel(villageDetails.getTownHallLevel());

        Village updatedVillage = villageRepository.save(villageToUpdate);
        return ResponseEntity.ok().body(updatedVillage);
    }

    @Operation(summary = "Deletar vila")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!villageRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        villageRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    //consulta
    @Operation(summary = "Filtrar por nível do Centro de Vila")
    @GetMapping(value = "/search")
    public ResponseEntity<List<Village>> searchByTownHallLevel(@RequestParam(name = "minLevel", defaultValue = "1") Integer minLevel) {
        List<Village> list = villageRepository.findByTownHallLevelGreaterThanEqual(minLevel);
        return ResponseEntity.ok().body(list);
    }
}