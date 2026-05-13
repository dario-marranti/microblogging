package com.microblogging.project.application.service;

import com.microblogging.project.adapter.in.web.dto.AgentSimulationRequest;
import com.microblogging.project.adapter.in.web.dto.AgentSimulationResult;
import com.microblogging.project.application.port.in.RunAgentSimulationUseCase;
import com.microblogging.project.domain.port.ai.AiAgentPort;
import com.microblogging.project.domain.port.ai.AiAgentPort.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Orquestador de simulación mejorado.
 * CORRECCIÓN: Ahora registra físicamente a los usuarios antes de intentar seguirlos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOrchestrationService implements RunAgentSimulationUseCase {

    private final AiAgentPort aiAgent;
    private final RestTemplate restTemplate;

    @org.springframework.beans.factory.annotation.Value("${agent.api.base-url:http://localhost:9090}")
    private String apiBaseUrl;

    @Override
    public AgentSimulationResult execute(AgentSimulationRequest request) {
        List<String> logs = new ArrayList<>();
        List<String> userIds = new ArrayList<>();
        int tweetsCreated = 0;
        int followsCreated = 0;

        // ── 1. Generar y REGISTRAR usuarios ──────────────────────────────────
        logs.add("=== FASE 1: Generando y registrando usuarios ===");
        Map<String, UserProfile> profileByUserId = new LinkedHashMap<>();

        for (int i = 0; i < request.numberOfUsers(); i++) {
            String userId = UUID.randomUUID().toString();
            UserProfile profile = aiAgent.generateUserProfile(i);

            // IMPORTANTE: Llamar al endpoint de registro para persistir el usuario
            boolean registered = callCreateUser(userId, profile);

            if (registered) {
                profileByUserId.put(userId, profile);
                userIds.add(userId);
                logs.add(String.format("✓ Usuario creado y persistido: id=%s username='%s'", userId, profile.username()));
            } else {
                logs.add(String.format("✗ Error persistiendo usuario: %s", profile.username()));
            }
        }

        // Pequeña pausa para asegurar consistencia en DB
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}

        // ── 2. Crear follows ─────────────────────────────────────────────────
        logs.add("=== FASE 2: Estableciendo follows ===");
        Random rng = new Random();
        List<String> userIdList = new ArrayList<>(userIds);

        for (String followerId : userIdList) {
            List<String> candidates = new ArrayList<>(userIdList);
            candidates.remove(followerId);
            Collections.shuffle(candidates, rng);

            int toFollow = Math.min(request.followsPerUser(), candidates.size());
            for (int i = 0; i < toFollow; i++) {
                String followeeId = candidates.get(i);
                boolean ok = callFollow(followerId, followeeId);
                if (ok) {
                    followsCreated++;
                    logs.add(String.format("✓ Follow: @%s → @%s",
                            profileByUserId.get(followerId).username(),
                            profileByUserId.get(followeeId).username()));
                } else {
                    logs.add(String.format("✗ Follow fallido: %s → %s (Verificar si el usuario existe en DB)", followerId, followeeId));
                }
            }
        }

        // ── 3. Publicar tweets ───────────────────────────────────────────────
        logs.add("=== FASE 3: Publicando tweets ===");
        for (Map.Entry<String, UserProfile> entry : profileByUserId.entrySet()) {
            String userId = entry.getKey();
            UserProfile profile = entry.getValue();

            List<String> tweets = aiAgent.generateTweets(profile.username(), request.topic(), request.tweetsPerUser());

            for (String content : tweets) {
                boolean ok = callPostTweet(userId, content);
                if (ok) {
                    tweetsCreated++;
                    logs.add(String.format("✓ Tweet de @%s: %s", profile.username(),
                            content.length() > 50 ? content.substring(0, 50) + "..." : content));
                }
            }
        }

        logs.add("=== SIMULACIÓN COMPLETADA ===");
        return new AgentSimulationResult(userIds.size(), tweetsCreated, followsCreated, userIds, logs);
    }

    // ── Helpers HTTP ──────────────────────────────────────────────────────────

    /**
     * Registra el usuario en el sistema llamando a POST /users
     */
    private boolean callCreateUser(String userId, UserProfile profile) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("id", userId);
            body.put("username", profile.username());
            body.put("bio", profile.bio());

            ResponseEntity<Void> response = restTemplate.postForEntity(apiBaseUrl + "/users", body, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error registrando usuario {}: {}", profile.username(), e.getMessage());
            return false;
        }
    }

    private boolean callFollow(String followerId, String followedId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", followerId);

            Map<String, String> body = Map.of("followeeId", followedId);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Void> response = restTemplate.exchange(apiBaseUrl + "/follow", HttpMethod.POST, entity, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Error en /follow: {}", e.getMessage());
            return false;
        }
    }

    private boolean callPostTweet(String userId, String content) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = Map.of("content", content.length() > 280 ? content.substring(0, 280) : content);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Void> response = restTemplate.exchange(apiBaseUrl + "/tweets", HttpMethod.POST, entity, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}