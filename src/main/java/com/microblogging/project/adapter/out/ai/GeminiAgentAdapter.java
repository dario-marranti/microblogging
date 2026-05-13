package com.microblogging.project.adapter.out.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microblogging.project.domain.port.ai.AiAgentPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Adaptador de salida que se comunica con la API de Google Gemini.
 * Migrado desde Claude para el proyecto de microblogging.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiAgentAdapter implements AiAgentPort {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String model;

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

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

        String raw = callGemini(prompt);

        try {
            String jsonOnly = extractJson(raw);
            JsonNode node = objectMapper.readTree(jsonOnly);

            // path() evita el NullPointerException si el campo no viene en el JSON
            String username = node.path("username").asText("user_" + index);
            String bio = node.path("bio").asText("Explorando el microblogging.");

            return new UserProfile(username, bio);
        } catch (Exception e) {
            log.warn("Error parseando perfil. Usando fallback. Raw: {}", raw);
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
                - Separalos ÚNICAMENTE con el delimitador: ---TWEET---
                
                Respondé solo con los tweets separados por ---TWEET---, sin nada más.
                """, username, count, topic);

        String raw = callGemini(prompt);
        if (raw == null || raw.isBlank()) return Collections.emptyList();

        String[] parts = raw.split("---TWEET---");
        List<String> tweets = new ArrayList<>();
        for (String part : parts) {
            String cleaned = part.strip();
            if (!cleaned.isEmpty()) {
                tweets.add(cleaned);
            }
        }

        return tweets.subList(0, Math.min(count, tweets.size()));
    }

    private String callGemini(String userPrompt) {
        try {
            String url = String.format("%s%s:generateContent?key=%s", GEMINI_BASE_URL, model, apiKey);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> textPart = Map.of("text", userPrompt);
            Map<String, Object> content = Map.of("parts", List.of(textPart));
            Map<String, Object> requestBody = Map.of("contents", List.of(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());

            // Navegación segura por el árbol de Gemini usando path()
            return root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text")
                    .asText("");

        } catch (Exception e) {
            log.error("Error en llamada a Gemini API: {}", e.getMessage());
            return "";
        }
    }

    private String extractJson(String text) {
        if (text == null || text.isBlank()) return "{}";

        // CORRECCIÓN: La regex ahora está en una sola línea para evitar el error de compilación
        return text.replaceAll("(?s) ` ` (?:json)?", "");
    }
}