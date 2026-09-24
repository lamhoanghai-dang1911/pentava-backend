package com.nqt.social.infrastructure.persistence.repository;

import com.nqt.social.infrastructure.persistence.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentJpaRepository extends JpaRepository<CommentEntity, Long> {

    List<CommentEntity> findByPostIdOrderByCreatedAtAsc(Long postId);

    long countByPostId(Long postId);
}
