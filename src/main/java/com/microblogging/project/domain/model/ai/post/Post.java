package com.microblogging.project.domain.model.ai.post;

import com.microblogging.project.domain.model.ai.user.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a microblogging post.
 */
public class Post {

    private static final int MAX_CONTENT_LENGTH = 280;

    private final String id;
    private final UserId authorId;
    private final String content;
    private final Instant createdAt;

    private Post(
            String id,
            UserId authorId,
            String content,
            Instant createdAt
    ) {
        this.id = id;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static Post create(
            UserId authorId,
            String content
    ) {

        validateContent(content);

        return new Post(
                UUID.randomUUID().toString(),
                authorId,
                content.trim(),
                Instant.now()
        );
    }

    private static void validateContent(String content) {

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Post content cannot be empty");
        }

        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException(
                    "Post content exceeds 280 characters"
            );
        }
    }

    public String getId() {
        return id;
    }

    public UserId getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post)) return false;
        Post post = (Post) o;
        return Objects.equals(id, post.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}