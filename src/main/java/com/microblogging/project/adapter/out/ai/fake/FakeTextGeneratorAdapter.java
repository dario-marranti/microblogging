package com.microblogging.project.adapter.out.ai.fake;

import com.microblogging.project.domain.port.ai.AITextGeneratorPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class FakeTextGeneratorAdapter implements AITextGeneratorPort {

    private static final List<String> POSTS = List.of(
            "Hexagonal Architecture keeps business rules isolated.",
            "Kafka is not a queue. Treat it as an event log.",
            "DDD is about language consistency."
    );

    private final Random random = new Random();

    @Override
    public String generateText(String prompt) {

        log.info("Generating fake AI content...");
        log.debug("Prompt received: {}", prompt);

        String generated = POSTS.get(random.nextInt(POSTS.size()));

        log.info("Generated content: {}", generated);

        return generated;
    }
}