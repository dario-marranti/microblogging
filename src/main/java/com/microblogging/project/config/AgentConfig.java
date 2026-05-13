package com.microblogging.project.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuración de beans necesarios para el módulo de agente de IA.
 *
 * Si tu proyecto ya tiene un RestTemplate o ObjectMapper configurados,
 * podés eliminar estos beans y usar los existentes con @Primary o @Qualifier.
 */
@Configuration
public class AgentConfig {
    @Bean
    public RestTemplate agentRestTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .build();
    }

    /** ObjectMapper estándar para parsear respuestas JSON del LLM. */
    @Bean
    public ObjectMapper agentObjectMapper() {
        return new ObjectMapper();
    }
}
