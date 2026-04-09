package com.allanhenrique.clashapi.controllers;

import com.allanhenrique.clashapi.entities.Clan;
import com.allanhenrique.clashapi.repositories.ClanRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;

@RestController
@RequestMapping(value = "/clans")
@Tag(name = "1. Clãs", description = "Gerenciamento central das alianças do jogo. Aqui você pode criar novos clãs, definir a quantidade mínima de troféus exigida para entrada e atualizar as informações do grupo. O Clã é a entidade raiz do sistema.")
public class ClanController {

    @Autowired
    private ClanRepository clanRepository;

    //Paginado (Buscar todos divididos em páginas)
    @Operation(summary = "Lista todos os clãs (HATEOAS)", description = "Retorna uma lista de clãs com links de navegação")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Clan>>> findAll
    (@ParameterObject @PageableDefault(page = 0, size = 10) Pageable pageable) {
        //buscando a página de clãs no banco de dados
        Page<Clan> clans = clanRepository.findAll(pageable);
        //para cada clã da lista, cria um EntityModel e adiciona um link individual self
        List<EntityModel<Clan>> clanModels = clans.stream()
                .map(clan -> EntityModel.of(clan,
                        linkTo(methodOn(ClanController.class).findById(clan.getId())).withSelfRel()))
                .toList();

        //cria o link para a própria listagem
        Link selfLink = linkTo(methodOn(ClanController.class).findAll(pageable)).withSelfRel();

        //retorna a coleção completa com os links
        return ResponseEntity.ok().body(CollectionModel.of(clanModels, selfLink));
    }

    @Operation(summary = "Busca clã por ID", description = "Retorna os detalhes de um clã específico baseado no seu ID")
    @ApiResponse(responseCode = "200", description = "Registro encontrado")
    @ApiResponse(responseCode = "400", description = "ID fornecido em formato inválido")
    @ApiResponse(responseCode = "404", description = "Registro não encontrado no banco")
    //buscando apenas um clã específico
    @GetMapping(value = "/{id}")
    public ResponseEntity<EntityModel<Clan>> findById(@PathVariable Long id) {
        Optional<Clan> obj = clanRepository.findById(id);

        if (obj.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Clan clan = obj.get();
        //link self link para este próprio clã
        Link selfLink = linkTo(methodOn(ClanController.class).findById(id)).withSelfRel();

        //link todos_clans
        Link allClansLink = linkTo(ClanController.class).withRel("todos_clans");

        //link deletar Atalho que já aponta para a rota de exclusão deste clã
        Link deleteLink = linkTo(methodOn(ClanController.class).delete(id)).withRel("deletar_este_clã");

        //monta o recurso Clã + Links
        EntityModel<Clan> resource = EntityModel.of(clan, selfLink, allClansLink, deleteLink);

        return ResponseEntity.ok().body(resource);
    }

    //criando um noovo clan
    @Operation(summary = "Criar novo clã que ainda nao existe")
    @ApiResponse(responseCode = "201", description = "Clã criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos enviados")
    @PostMapping
    public ResponseEntity<Clan> insert(@Valid @RequestBody Clan clan) {
        Clan savedClan = clanRepository.save(clan);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedClan);
    }

    //atualizando um clan que ja existe criado e somente mudando os dados
    @Operation(summary = "Atualizar um clã que ja foi criado")
    @ApiResponse(responseCode = "200", description = "clã atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    @ApiResponse(responseCode = "404", description = "Registro não encontrado para atualização desse clã")
    @PutMapping(value = "/{id}")
    public ResponseEntity<Clan> update(@PathVariable Long id,@Valid @RequestBody Clan clanDetails) {
        Optional<Clan> obj = clanRepository.findById(id);
        if (obj.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Clan clanToUpdate = obj.get();
        clanToUpdate.setName(clanDetails.getName());
        clanToUpdate.setDescription(clanDetails.getDescription());
        clanToUpdate.setRequiredTrophies(clanDetails.getRequiredTrophies());

        Clan updatedClan = clanRepository.save(clanToUpdate);
        return ResponseEntity.ok().body(updatedClan);
    }
    //Excluindo um clan
    @Operation(summary = "Deletar clã")
    @ApiResponse(responseCode = "204", description = "clã excluído com sucesso")
    @ApiResponse(responseCode = "400", description = "ID do clã inválido")
    @ApiResponse(responseCode = "404", description = "Registro do clã não encontrado")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!clanRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        clanRepository.deleteById(id);
        return ResponseEntity.noContent().build(); // Retorna 204 informando que foi delatado com sucesso o clan
    }
    //consulta de trofeus minimos
    @Operation(summary = "Filtrar clãs por troféus mínimos")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros de busca inválidos")
    @GetMapping(value = "/search")
    public ResponseEntity<List<Clan>> searchByTrophies(@RequestParam(name = "minTrophies", defaultValue = "0") Integer minTrophies) {
        List<Clan> list = clanRepository.findByRequiredTrophiesGreaterThanEqual(minTrophies);
        return ResponseEntity.ok().body(list);
    }
}