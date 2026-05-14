package com.microblogging.project.adapter.in.web.controller;

import com.microblogging.project.adapter.in.web.dto.AgentSimulationRequest;
import com.microblogging.project.adapter.in.web.dto.AgentSimulationResult;
import com.microblogging.project.application.port.in.RunAgentSimulationUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el agente de IA.
 *
 * POST /agent/simulate   → lanza una simulación completa
 * GET  /agent/status     → estado rápido del servicio
 */
@Slf4j
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final RunAgentSimulationUseCase runAgentSimulationUseCase;

    /**
     * Dispara la simulación del agente.
     * El agente creará usuarios, establecerá relaciones de follow
     * y publicará tweets generados por IA sobre el topic indicado.
     */
    @PostMapping("/simulate")
    public ResponseEntity<AgentSimulationResult> simulate(
            @Valid @RequestBody AgentSimulationRequest request) {

        log.info("Iniciando simulación: {} usuarios, {} tweets/usuario, topic='{}'",
                request.numberOfUsers(), request.tweetsPerUser(), request.topic());

        AgentSimulationResult result = runAgentSimulationUseCase.execute(request);

        log.info("Simulación completada: {} usuarios, {} tweets, {} follows",
                result.usersCreated(), result.tweetsCreated(), result.followsCreated());

        return ResponseEntity.ok(result);
    }

    /** Health-check rápido del módulo de agente. */
    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("Agent module is active");
    }
}
