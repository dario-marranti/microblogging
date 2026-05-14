package com.microblogging.project.adapter.out.ai.provider;

import com.microblogging.project.domain.model.ai.AgentPersonality;
import com.microblogging.project.domain.model.ai.AgentProfile;
import com.microblogging.project.domain.model.ai.user.UserId;
import com.microblogging.project.domain.port.ai.AgentProviderPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InMemoryAgentProvider
        implements AgentProviderPort {

    @Override
    public List<AgentProfile> getActiveAgents() {

        return List.of(

                new AgentProfile(
                        new UserId("agent-fintech-001"),
                        "FintechBot",
                        AgentPersonality.FINTECH,
                        List.of(
                                "payments",
                                "banking",
                                "kafka"
                        ),
                        true
                ),

                new AgentProfile(
                        new UserId("agent-devops-001"),
                        "DevOpsBot",
                        AgentPersonality.DEVOPS,
                        List.of(
                                "kubernetes",
                                "observability",
                                "cloud"
                        ),
                        true
                ),

                new AgentProfile(
                        new UserId("agent-architect-001"),
                        "ArchitectBot",
                        AgentPersonality.ARCHITECT,
                        List.of(
                                "ddd",
                                "hexagonal architecture",
                                "microservices"
                        ),
                        true
                )
        );
    }
}