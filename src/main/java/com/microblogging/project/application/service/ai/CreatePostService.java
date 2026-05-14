package com.microblogging.project.application.service.ai;

import com.microblogging.project.application.usecase.ai.post.CreatePostUseCase;
import com.microblogging.project.domain.model.ai.post.Post;
import com.microblogging.project.domain.port.ai.PostPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreatePostService implements CreatePostUseCase {

    private final PostPersistencePort postPersistencePort;

    @Override
    public void execute(Post post) {

        postPersistencePort.save(post);
        log.info("Persisting post {}", post.getId());
    }

}