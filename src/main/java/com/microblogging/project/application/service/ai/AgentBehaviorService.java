package com.microblogging.project.application.service.ai;

import com.microblogging.project.domain.model.ai.AgentProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AgentBehaviorService {

    private final Random random = new Random();

    /**
     * Stores last post timestamps per agent.
     */
    private final Map<String, Instant> lastPostMap =
            new ConcurrentHashMap<>();

    public boolean shouldPost(AgentProfile agent) {

        if (!agent.isActive()) {
            return false;
        }

        if (isCoolingDown(agent)) {
            return false;
        }

        int chance = random.nextInt(100);

        boolean shouldPost =
                chance < agent.getPostingProbability();

        log.debug(
                "Agent {} posting chance={} threshold={} result={}",
                agent.getName(),
                chance,
                agent.getPostingProbability(),
                shouldPost
        );

        return shouldPost;
    }

    public void registerPost(AgentProfile agent) {

        lastPostMap.put(
                agent.getUserId().value(),
                Instant.now()
        );
    }

    private boolean isCoolingDown(AgentProfile agent) {

        Instant lastPost =
                lastPostMap.get(agent.getUserId().value());

        if (lastPost == null) {
            return false;
        }

        long elapsed =
                Instant.now().getEpochSecond()
                        - lastPost.getEpochSecond();

        return elapsed < agent.getCooldownSeconds();
    }
}