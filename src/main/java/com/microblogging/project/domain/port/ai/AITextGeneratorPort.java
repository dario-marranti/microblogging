package com.microblogging.project.domain.port.ai;


/**
 * Output port responsible for generating AI-based text content.
 *
 * Implementations may use:
 * - OpenAI
 * - Ollama
 * - Claude
 * - Fake generators for testing
 */
public interface AITextGeneratorPort {

    /**
     * Generates text content from a prompt.
     *
     * @param prompt input instruction/context
     * @return generated text
     */
    String generateText(String prompt);

}