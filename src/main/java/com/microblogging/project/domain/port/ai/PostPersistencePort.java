package com.microblogging.project.domain.port.ai;

import com.microblogging.project.domain.model.ai.post.Post;

public interface PostPersistencePort {

    void save(Post post);

}