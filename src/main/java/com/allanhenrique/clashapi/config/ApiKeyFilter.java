package com.allanhenrique.clashapi.config;

import com.allanhenrique.clashapi.repositories.ApiKeyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    // Rotas que NÃO precisam de autenticação
    private static final String[] PUBLIC_ROUTES = {
            "/api-keys",          // geração de chaves
            "/swagger-ui",        // interface do Swagger
            "/v3/api-docs",       // documentação OpenAPI
            "/swagger-resources", // recursos do Swagger
            "/webjars",           // assets do Swagger
            "/greetings"          // endpoint de teste
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Libera requisições OPTIONS (CORS Preflight) para todas as rotas
        // O navegador não envia chaves de API durante o Preflight, então precisamos deixar passar.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // 2. Verifica se a rota é pública — se for, deixa passar sem autenticar
        for (String publicRoute : PUBLIC_ROUTES) {
            if (path.startsWith(publicRoute)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 3. Lê o header X-API-Key da requisição para rotas protegidas
        String apiKey = request.getHeader("X-API-Key");

        // Se não veio o header, retorna 401
        if (apiKey == null || apiKey.isBlank()) {
            sendUnauthorized(response, "Header X-API-Key é obrigatório.", request.getRequestURI());
            return;
        }

        // 4. Busca a chave no banco — só aceita se existir E estiver ativa
        boolean valid = apiKeyRepository.findByKeyAndActiveTrue(apiKey).isPresent();

        if (!valid) {
            sendUnauthorized(response, "X-API-Key inválida ou revogada.", request.getRequestURI());
            return;
        }

        // Chave válida — deixa a requisição continuar para o controller
        filterChain.doFilter(request, response);
    }

    // Monta o JSON de erro 401 no mesmo padrão do ResourceExceptionHandler
    private void sendUnauthorized(HttpServletResponse response, String message, String path) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", Instant.now().toString());
        error.put("status", 401);
        error.put("error", "Não autorizado");
        error.put("message", message);
        error.put("path", path);

        new ObjectMapper().writeValue(response.getWriter(), error);
    }
}