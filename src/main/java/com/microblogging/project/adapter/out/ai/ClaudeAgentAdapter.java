package com.microblogging.project.adapter.out.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microblogging.project.application.port.out.AiAgentPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Adaptador de salida que se comunica con la API de Anthropic (Claude).
 *
 * Configura la API key en application.properties:
 *   anthropic.api.key=sk-ant-...
 *
 * Usa el modelo claude-haiku (rápido y económico) para generación masiva.
 * Podés cambiarlo a claude-sonnet para mayor calidad.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClaudeAgentAdapter implements AiAgentPort {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.model:claude-haiku-4-5-20251001}")
    private String model;

    private static final String ANTHROPIC_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_VERSION   = "2023-06-01";

    // ── AiAgentPort ───────────────────────────────────────────────────────────

    @Override
    public UserProfile generateUserProfile(int index) {
        String prompt = String.format("""
                Generá un perfil de usuario ficticio para una red social de microblogging.
                El usuario número %d debe tener una personalidad única e interesante.
                
                Respondé ÚNICAMENTE con JSON válido, sin explicaciones, con este formato exacto:
                {
                  "username": "nombre_usuario_sin_espacios",
                  "bio": "Bio del usuario de máximo 100 caracteres"
                }
                """, index + 1);

        String raw = callClaude(prompt, 200);

        try {
            JsonNode node = objectMapper.readTree(extractJson(raw));
            return new UserProfile(
                    node.get("username").asText(),
                    node.get("bio").asText()
            );
        } catch (Exception e) {
            log.warn("No se pudo parsear el perfil generado, usando fallback. Raw: {}", raw);
            return new UserProfile("user_" + index, "Usuario generado por IA #" + index);
        }
    }

    @Override
    public List<String> generateTweets(String username, String topic, int count) {
        if (count <= 0) return Collections.emptyList();

        String prompt = String.format("""
                Sos @%s, un usuario de microblogging.
                Generá exactamente %d tweets sobre el tema: "%s".
                
                Reglas estrictas:
                - Cada tweet tiene MÁXIMO 280 caracteres
                - Usá voz natural, informal, con personalidad propia
                - Podés incluir hashtags y emojis con moderación
                - NO numeres los tweets
                - Separalos ÚNICAMENTE con el delimitador: ---TWEET---
                
                Respondé solo con los tweets separados por ---TWEET---, sin nada más.
                """, username, count, topic);

        String raw = callClaude(prompt, 600);

        String[] parts = raw.split("---TWEET---");
        List<String> tweets = new ArrayList<>();
        for (String part : parts) {
            String cleaned = part.strip();
            if (!cleaned.isEmpty()) {
                tweets.add(cleaned);
            }
        }

        // Si Claude devolvió más o menos, ajustamos
        return tweets.subList(0, Math.min(count, tweets.size()));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Realiza una llamada simple a la API de Anthropic Messages.
     * @param userPrompt  el mensaje del usuario
     * @param maxTokens   tokens máximos en la respuesta
     * @return texto de la respuesta del modelo
     */
    private String callClaude(String userPrompt, int maxTokens) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", API_VERSION);

            Map<String, Object> message = Map.of(
                    "role", "user",
                    "content", userPrompt
            );

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("messages", List.of(message));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    ANTHROPIC_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode root    = objectMapper.readTree(response.getBody());
            JsonNode content = root.path("content");

            if (content.isArray() && !content.isEmpty()) {
                return content.get(0).path("text").asText("");
            }

            log.warn("Respuesta inesperada de Claude: {}", response.getBody());
            return "";

        } catch (Exception e) {
            log.error("Error llamando a la API de Anthropic: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * Extrae el bloque JSON de una respuesta que puede incluir texto antes/después.
     * Busca la primera { y el último } para aislar el JSON.
     */
    private String extractJson(String text) {
        if (text == null || text.isBlank()) return "{}";
        int start = text.indexOf('{');
        int end   = text.lastIndexOf('}');
        if (start == -1 || end == -1 || start >= end) return "{}";
        return text.substring(start, end + 1);
    }
}
