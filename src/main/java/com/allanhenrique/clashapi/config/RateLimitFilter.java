package com.allanhenrique.clashapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // Limite de requisições por janela de tempo
    private static final int MAX_REQUESTS = 20;

    // Janela de tempo em milissegundos (1 minuto)
    private static final long TIME_WINDOW_MS = 60_000;

    // Segundos para o cliente esperar antes de tentar novamente
    private static final int RETRY_AFTER_SECONDS = 60;

    // Mapa em memória: IP → dados de controle (contador + timestamp da janela)
    // ConcurrentHashMap garante segurança em requisições simultâneas
    private final ConcurrentHashMap<String, RequestCount> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Pega o IP real do cliente (considera proxies reversos)
        String clientIp = getClientIp(request);

        // Busca ou cria o contador para esse IP
        RequestCount count = requestCounts.computeIfAbsent(clientIp, k -> new RequestCount());

        long now = System.currentTimeMillis();

        synchronized (count) {
            // Se a janela de tempo expirou, reinicia o contador
            if (now - count.windowStart > TIME_WINDOW_MS) {
                count.windowStart = now;
                count.counter.set(0);
            }

            // Incrementa o contador e verifica o limite
            int currentCount = count.counter.incrementAndGet();

            // Adiciona headers informativos em todas as respostas
            response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, MAX_REQUESTS - currentCount)));
            response.setHeader("X-RateLimit-Reset", String.valueOf((count.windowStart + TIME_WINDOW_MS) / 1000));

            // Se passou do limite, retorna 429
            if (currentCount > MAX_REQUESTS) {
                response.setHeader("Retry-After", String.valueOf(RETRY_AFTER_SECONDS));
                sendTooManyRequests(response, clientIp, request.getRequestURI());
                return;
            }
        }

        // Dentro do limite — deixa a requisição continuar
        filterChain.doFilter(request, response);
    }

    // Pega o IP real do cliente, considerando proxies reversos (ex: Nginx, Render.com)
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For pode ter múltiplos IPs separados por vírgula — pega o primeiro
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // Monta o JSON de erro 429 no mesmo padrão do ResourceExceptionHandler
    private void sendTooManyRequests(HttpServletResponse response, String ip, String path) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", Instant.now().toString());
        error.put("status", 429);
        error.put("error", "Too Many Requests");
        error.put("message", "Limite de " + MAX_REQUESTS + " requisições por minuto excedido. Tente novamente em " + RETRY_AFTER_SECONDS + " segundos.");
        error.put("path", path);

        new ObjectMapper().writeValue(response.getWriter(), error);
    }

    // Classe interna para guardar o contador e o início da janela de tempo por IP
    private static class RequestCount {
        AtomicInteger counter = new AtomicInteger(0);
        long windowStart = System.currentTimeMillis();
    }
}