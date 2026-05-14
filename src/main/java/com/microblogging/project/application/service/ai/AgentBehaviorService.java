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

    private final Map<String, Instant> lastPostMap = new ConcurrentHashMap<>();

    /**
     * Emotional/energy state per agent.
     * Simulates "mood" or "activity level".
     */
    private final Map<String, Integer> energyMap = new ConcurrentHashMap<>();

    public boolean shouldPost(AgentProfile agent) {

        if (!agent.isActive()) {
            return false;
        }

        int energy = getEnergy(agent);

        if (isCoolingDown(agent)) {
            return false;
        }

        // Base probability from profile
        int baseChance = agent.getPostingProbability();

        // Energy influences behavior
        int adjustedChance = baseChance + (energy / 2);

        int roll = random.nextInt(100);

        boolean decision = roll < adjustedChance;

        log.debug(
                "Agent {} | roll={} threshold={} energy={}",
                agent.getName(),
                roll,
                adjustedChance,
                energy
        );

        return decision;
    }

    public void registerPost(AgentProfile agent) {

        String id = agent.getUserId().value();

        lastPostMap.put(id, Instant.now());

        // Posting consumes energy
        energyMap.put(id, Math.max(0, getEnergy(agent) - 30));
    }

    /**
     * Simulates gradual energy recovery over time.
     */
    public void tick() {

        energyMap.forEach((id, energy) -> {

            int newEnergy = Math.min(100, energy + random.nextInt(10));

            energyMap.put(id, newEnergy);

            log.debug("Agent {} energy updated to {}", id, newEnergy);
        });
    }

    private int getEnergy(AgentProfile agent) {

        return energyMap.computeIfAbsent(
                agent.getUserId().value(),
                id -> 70 + random.nextInt(30) // initial energy
        );
    }

    private boolean isCoolingDown(AgentProfile agent) {

        Instant last = lastPostMap.get(agent.getUserId().value());

        if (last == null) {
            return false;
        }

        long elapsed =
                Instant.now().getEpochSecond()
                        - last.getEpochSecond();

        return elapsed < agent.getCooldownSeconds();
    }
}