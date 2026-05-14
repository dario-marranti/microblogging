package com.microblogging.project.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "posts")
@Getter
@Setter
public class PostEntity {

    @Id
    private String id;

    private String authorId;

    private String content;

    private Instant createdAt;

    // getters/setters
}