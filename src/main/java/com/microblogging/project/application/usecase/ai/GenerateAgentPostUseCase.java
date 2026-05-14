package com.microblogging.project.application.usecase.ai;

import com.microblogging.project.domain.model.ai.AgentProfile;
import com.microblogging.project.domain.port.ai.AITextGeneratorPort;
import com.microblogging.project.application.usecase.ai.post.CreatePostUseCase;
import com.microblogging.project.domain.model.ai.post.Post;
import com.microblogging.project.domain.model.ai.user.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateAgentPostUseCase {

    private final AITextGeneratorPort aiTextGeneratorPort;
    private final CreatePostUseCase createPostUseCase;

    public void generateAndPublish(AgentProfile profile) {

        log.info(
                "Generating AI post for agent {}",
                profile.getName()
        );

        String prompt = buildPrompt(profile);

        String content =
                aiTextGeneratorPort.generateText(prompt);

        Post post = Post.create(
                profile.getUserId(),
                content
        );

        createPostUseCase.execute(post);

        log.info(
                "AI post published for agent {}",
                profile.getName()
        );
    }
    private String buildPrompt(AgentProfile profile) {
        return """
                Generate a short microblogging post about:
                - backend development
                - system design
                - Java
                - distributed systems

                Max 280 characters.
                """;
    }
}