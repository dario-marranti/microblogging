package com.microblogging.project.domain.model.ai;

import com.microblogging.project.domain.model.ai.user.UserId;

import java.util.List;
import java.util.Objects;

public class AgentProfile {

    private final UserId userId;

    private final String name;

    private final AgentPersonality personality;

    private final List<String> interests;

    private final boolean active;

    private final int postingProbability;

    private final long cooldownSeconds;

    public AgentProfile(
            UserId userId,
            String name,
            AgentPersonality personality,
            List<String> interests,
            boolean active,
            int postingProbability,
            long cooldownSeconds
    ) {
        this.userId = userId;
        this.name = name;
        this.personality = personality;
        this.interests = interests;
        this.active = active;
        this.postingProbability = postingProbability;
        this.cooldownSeconds = cooldownSeconds;
    }

    public UserId getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public AgentPersonality getPersonality() {
        return personality;
    }

    public List<String> getInterests() {
        return interests;
    }

    public boolean isActive() {
        return active;
    }

    public int getPostingProbability() {
        return postingProbability;
    }

    public long getCooldownSeconds() {
        return cooldownSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgentProfile)) return false;
        AgentProfile that = (AgentProfile) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}