package com.microblogging.project.adapter.in.web.dto;

import java.util.List;

/**
 * Resultado devuelto al cliente al completar la simulación.
 */
public record AgentSimulationResult(
        int usersCreated,
        int tweetsCreated,
        int followsCreated,
        List<String> userIds,
        List<String> log
) {}
