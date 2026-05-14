package com.microblogging.project.adapter.out.persistence;

import com.microblogging.project.adapter.out.persistence.mapper.PostMapper;
import com.microblogging.project.adapter.out.persistence.repository.PostJpaRepository;
import com.microblogging.project.domain.model.ai.post.Post;
import com.microblogging.project.domain.port.ai.PostPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostPersistenceAdapter implements PostPersistencePort {

    private final PostJpaRepository repository;

    @Override
    public void save(Post post) {
        repository.save(PostMapper.toEntity(post));
        log.info("Saving post entity into database");
    }
}