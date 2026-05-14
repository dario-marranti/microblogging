package com.microblogging.project.adapter.in.web.controller;

import com.microblogging.project.application.usecase.ai.GenerateAgentPostUseCase;
import com.microblogging.project.domain.model.ai.AgentProfile;
import com.microblogging.project.domain.model.ai.user.UserId;
import com.microblogging.project.domain.port.ai.AgentProviderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final GenerateAgentPostUseCase generateAgentPostUseCase;

    private final AgentProviderPort agentProviderPort;

    @PostMapping("/generate/{agentId}")
    public void generate(
            @PathVariable String agentId
    ) {

        AgentProfile agent =
                agentProviderPort
                        .getActiveAgents()
                        .stream()
                        .filter(a ->
                                a.getUserId()
                                        .value()
                                        .equals(agentId)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Agent not found: " + agentId
                                )
                        );

        generateAgentPostUseCase
                .generateAndPublish(agent);
    }
}