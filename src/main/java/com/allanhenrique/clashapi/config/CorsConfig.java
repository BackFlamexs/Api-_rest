package com.allanhenrique.clashapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // aplica para todos os endpoints
                .allowedOrigins(
                        "http://localhost:3000",   // React/Next.js local
                        "http://localhost:4200",   // Angular local
                        "http://localhost:8080",   // própria API (Swagger)
                        "http://localhost:5173",   // Antigravity / Vite (Novo Front-end)
                        "https://clash-api.onrender.com" // URL de produção
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Idempotency-Key", "X-API-Key", "X-API-Version")
                .allowCredentials(true)
                .maxAge(3600); // cache do preflight por 1 hora
    }
}