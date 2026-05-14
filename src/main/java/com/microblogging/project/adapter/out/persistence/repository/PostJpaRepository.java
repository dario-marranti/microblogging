package com.microblogging.project.adapter.out.persistence.repository;

import com.microblogging.project.adapter.out.persistence.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostJpaRepository extends JpaRepository<PostEntity, String> {
}