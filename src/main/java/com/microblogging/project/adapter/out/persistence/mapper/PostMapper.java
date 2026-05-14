package com.microblogging.project.adapter.out.persistence.mapper;

import com.microblogging.project.adapter.out.persistence.entity.PostEntity;
import com.microblogging.project.domain.model.ai.post.Post;

public class PostMapper {

    public static PostEntity toEntity(Post post) {

        PostEntity entity = new PostEntity();
        entity.setId(post.getId());
        entity.setAuthorId(post.getAuthorId().value());
        entity.setContent(post.getContent());
        entity.setCreatedAt(post.getCreatedAt());

        return entity;
    }
}