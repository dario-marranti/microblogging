package com.microblogging.project.scheduler;

import com.microblogging.project.application.service.ai.AgentBehaviorService;
import com.microblogging.project.application.usecase.ai.GenerateAgentPostUseCase;
import com.microblogging.project.domain.port.ai.AgentProviderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentScheduler {

    private final GenerateAgentPostUseCase generateAgentPostUseCase;

    private final AgentProviderPort agentProviderPort;

    private final AgentBehaviorService agentBehaviorService;

    @Scheduled(fixedRate = 30000)
    public void publishAgentPosts() {

        var agents = agentProviderPort.getActiveAgents();

        agents.forEach(agent -> {

            if (agentBehaviorService.shouldPost(agent)) {

                log.info(
                        "Agent {} decided to publish",
                        agent.getName()
                );

                generateAgentPostUseCase
                        .generateAndPublish(agent);

                agentBehaviorService.registerPost(agent);

            } else {

                log.debug(
                        "Agent {} skipped publishing",
                        agent.getName()
                );

            }
        });
    }
}