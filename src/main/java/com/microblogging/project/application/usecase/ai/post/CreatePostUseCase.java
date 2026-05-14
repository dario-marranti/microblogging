package com.microblogging.project.application.usecase.ai.post;

import com.microblogging.project.domain.model.ai.post.Post;

public interface CreatePostUseCase {

    void execute(Post post);

}