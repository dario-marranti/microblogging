package com.microblogging.project.scheduler;

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

    @Scheduled(fixedRate = 30000)
    public void publishAgentPosts() {

        var agents = agentProviderPort.getActiveAgents();

        log.info(
                "Starting AI generation for {} agents",
                agents.size()
        );

        agents.forEach(generateAgentPostUseCase::generateAndPublish
        );

        log.info("Finished scheduled AI generation");
    }
}