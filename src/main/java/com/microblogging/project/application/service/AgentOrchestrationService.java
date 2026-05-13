package com.microblogging.project.application.service;

import com.microblogging.project.adapter.in.web.dto.AgentSimulationRequest;
import com.microblogging.project.adapter.in.web.dto.AgentSimulationResult;
import com.microblogging.project.application.port.in.RunAgentSimulationUseCase;
import com.microblogging.project.application.port.out.AiAgentPort;
import com.microblogging.project.application.port.out.AiAgentPort.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Orquesta la simulación completa:
 * 1. Genera perfiles de usuario con IA
 * 2. Registra los usuarios en el sistema (via endpoints existentes)
 * 3. Crea relaciones de follow aleatorias
 * 4. Genera y publica tweets con IA
 *
 * Llama a los endpoints REST internos del propio microblogging
 * para reutilizar toda la lógica de negocio existente.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOrchestrationService implements RunAgentSimulationUseCase {

    private final AiAgentPort aiAgent;
    private final RestTemplate restTemplate;

    /** URL base de la propia API — se inyecta desde application.properties */
    @org.springframework.beans.factory.annotation.Value("${agent.api.base-url:http://localhost:9090}")
    private String apiBaseUrl;

    @Override
    public AgentSimulationResult execute(AgentSimulationRequest request) {

        List<String> log     = new ArrayList<>();
        List<String> userIds = new ArrayList<>();
        int tweetsCreated    = 0;
        int followsCreated   = 0;

        // ── 1. Crear usuarios ────────────────────────────────────────────────
        log.add("=== FASE 1: Generando usuarios ===");
        Map<String, UserProfile> profileByUserId = new LinkedHashMap<>();

        for (int i = 0; i < request.numberOfUsers(); i++) {
            String userId = UUID.randomUUID().toString();
            UserProfile profile = aiAgent.generateUserProfile(i);

            profileByUserId.put(userId, profile);
            userIds.add(userId);

            log.add(String.format("✓ Usuario creado: id=%s username='%s' bio='%s'",
                    userId, profile.username(), profile.bio()));
        }

        // ── 2. Crear follows ─────────────────────────────────────────────────
        log.add("=== FASE 2: Estableciendo follows ===");
        Random rng = new Random();
        List<String> userIdList = new ArrayList<>(userIds);

        for (String followerId : userIdList) {
            // Elegir followsPerUser usuarios distintos al azar
            List<String> candidates = new ArrayList<>(userIdList);
            candidates.remove(followerId);
            Collections.shuffle(candidates, rng);

            int toFollow = Math.min(request.followsPerUser(), candidates.size());
            for (int i = 0; i < toFollow; i++) {
                String followeeId = candidates.get(i);
                boolean ok = callFollow(followerId, followeeId);
                if (ok) {
                    followsCreated++;
                    log.add(String.format("✓ Follow: %s → %s",
                            profileByUserId.get(followerId).username(),
                            profileByUserId.get(followeeId).username()));
                } else {
                    log.add(String.format("✗ Follow fallido: %s → %s", followerId, followeeId));
                }
            }
        }

        // ── 3. Publicar tweets ───────────────────────────────────────────────
        log.add("=== FASE 3: Publicando tweets ===");
        for (Map.Entry<String, UserProfile> entry : profileByUserId.entrySet()) {
            String userId      = entry.getKey();
            UserProfile profile = entry.getValue();

            List<String> tweets = aiAgent.generateTweets(
                    profile.username(), request.topic(), request.tweetsPerUser());

            for (String content : tweets) {
                boolean ok = callPostTweet(userId, content);
                if (ok) {
                    tweetsCreated++;
                    log.add(String.format("✓ Tweet de @%s: %s", profile.username(),
                            content.length() > 60 ? content.substring(0, 60) + "…" : content));
                } else {
                    log.add(String.format("✗ Tweet fallido para @%s", profile.username()));
                }
            }
        }

        log.add("=== SIMULACIÓN COMPLETADA ===");

        return new AgentSimulationResult(
                userIds.size(),
                tweetsCreated,
                followsCreated,
                userIds,
                log
        );
    }

    // ── Helpers HTTP ──────────────────────────────────────────────────────────

    /**
     * POST /follow con header X-User-Id = followerId y body { "followeeId": "..." }
     * Retorna true si la respuesta fue 2xx.
     */
    private boolean callFollow(String followerId, String followeeId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", followerId);

            Map<String, String> body = Map.of("followeeId", followeeId);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    apiBaseUrl + "/follow",
                    HttpMethod.POST,
                    entity,
                    Void.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Error al llamar /follow: {}", e.getMessage());
            return false;
        }
    }

    /**
     * POST /tweets con header X-User-Id = userId y body { "content": "..." }
     * Retorna true si la respuesta fue 2xx.
     */
    private boolean callPostTweet(String userId, String content) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", userId);

            // Respetar límite de 280 caracteres de tu dominio
            String safeContent = content.length() > 280
                    ? content.substring(0, 277) + "..."
                    : content;

            Map<String, String> body = Map.of("content", safeContent);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    apiBaseUrl + "/tweets",
                    HttpMethod.POST,
                    entity,
                    Void.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Error al llamar /tweets: {}", e.getMessage());
            return false;
        }
    }
}
