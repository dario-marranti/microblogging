package com.microblogging.project.domain.port.ai;

import com.microblogging.project.domain.model.ai.AgentProfile;

import java.util.List;

public interface AgentProviderPort {

    List<AgentProfile> getActiveAgents();

}