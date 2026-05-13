package com.microblogging.project.application.port.out;

import java.util.List;

/**
 * Puerto de salida (driven port) que abstrae la comunicación con el LLM.
 * La implementación concreta (ClaudeAgentAdapter) vive en adapter/out.
 */
public interface AiAgentPort {

    /**
     * Genera un perfil de usuario ficticio con username y bio.
     * @param index número de usuario (para variedad)
     * @return UserProfile con username y bio
     */
    UserProfile generateUserProfile(int index);

    /**
     * Genera una lista de tweets para un usuario dado su perfil y el topic.
     * Cada tweet respeta el límite de 280 caracteres.
     *
     * @param username     nombre del usuario autor
     * @param topic        tema sobre el que twittear
     * @param count        cantidad de tweets a generar
     * @return lista de contenidos de tweets
     */
    List<String> generateTweets(String username, String topic, int count);

    /** Modelo de perfil generado por IA */
    record UserProfile(String username, String bio) {}
}
