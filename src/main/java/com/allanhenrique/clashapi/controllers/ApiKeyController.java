package com.allanhenrique.clashapi.controllers;

import com.allanhenrique.clashapi.entities.ApiKey;
import com.allanhenrique.clashapi.repositories.ApiKeyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api-keys")
@Tag(name = "0. Autenticação", description = "Gerenciamento de chaves de API. Gere uma chave aqui e use-a no header 'X-API-Key' em todas as outras requisições.")
public class ApiKeyController {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Operation(
            summary = "Gerar nova chave de API",
            description = "Gera uma chave de API única para um usuário. Esta é a única rota que NÃO exige autenticação. Use a chave gerada no header 'X-API-Key' em todas as outras requisições."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Chave gerada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Nome do dono é obrigatório")
    })
    @PostMapping
    public ResponseEntity<ApiKey> generate(@RequestParam String owner) {
        if (owner == null || owner.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome do dono é obrigatório.");
        }

        // Gera uma chave única no formato: clash-<UUID>
        String generatedKey = "clash-" + UUID.randomUUID().toString().replace("-", "");

        ApiKey apiKey = new ApiKey(generatedKey, owner, true, java.time.Instant.now());
        ApiKey saved = apiKeyRepository.save(apiKey);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(
            summary = "Listar todas as chaves de API",
            description = "Lista todas as chaves cadastradas. Requer header 'X-API-Key' válido."
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @ApiResponse(responseCode = "401", description = "X-API-Key ausente ou inválida")
    @GetMapping
    public ResponseEntity<List<ApiKey>> findAll() {
        return ResponseEntity.ok().body(apiKeyRepository.findAll());
    }

    @Operation(
            summary = "Revogar chave de API",
            description = "Desativa uma chave de API. A chave continua no banco mas não poderá mais ser usada. Requer header 'X-API-Key' válido."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chave revogada com sucesso"),
            @ApiResponse(responseCode = "401", description = "X-API-Key ausente ou inválida"),
            @ApiResponse(responseCode = "404", description = "Chave não encontrada")
    })
    @DeleteMapping(value = "/{key}")
    public ResponseEntity<ApiKey> revoke(@PathVariable String key) {
        ApiKey apiKey = apiKeyRepository.findById(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chave não encontrada."));

        apiKey.setActive(false);
        apiKeyRepository.save(apiKey);

        return ResponseEntity.ok().body(apiKey);
    }
}