package com.microblogging.project.application.port.in;

import com.microblogging.project.adapter.in.web.dto.AgentSimulationRequest;
import com.microblogging.project.adapter.in.web.dto.AgentSimulationResult;

/**
 * Puerto de entrada (driving port) para ejecutar la simulación del agente.
 * El controlador llama a este use case; la implementación vive en application layer.
 */
public interface RunAgentSimulationUseCase {
    AgentSimulationResult execute(AgentSimulationRequest request);
}
