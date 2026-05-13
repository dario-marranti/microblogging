package com.microblogging.project.adapter.in.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Request para configurar la simulación del agente de IA.
 * Todos los campos son opcionales; se usan defaults si no se envían.
 */
public record AgentSimulationRequest(

        @Min(1) @Max(20)
        Integer numberOfUsers,       // default: 5

        @Min(0) @Max(50)
        Integer tweetsPerUser,       // default: 3

        @Min(0) @Max(10)
        Integer followsPerUser,      // default: 2

        String topic                 // tema libre para los tweets, ej: "tecnología", "fútbol"
) {
    public AgentSimulationRequest {
        if (numberOfUsers == null) numberOfUsers = 5;
        if (tweetsPerUser  == null) tweetsPerUser  = 3;
        if (followsPerUser == null) followsPerUser = 2;
        if (topic == null || topic.isBlank()) topic = "tecnología y cultura";
    }
}
