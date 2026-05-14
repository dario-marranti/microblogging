package com.microblogging.project.config;

import com.microblogging.project.adapter.out.ai.fake.FakeTextGeneratorAdapter;
import com.microblogging.project.domain.port.ai.AITextGeneratorPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfiguration {

    @Bean
    public AITextGeneratorPort aiTextGeneratorPort() {
        return new FakeTextGeneratorAdapter();
    }

}