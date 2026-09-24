package com.nqt.social.infrastructure.persistence.adapter;

import com.nqt.social.domain.Comment;
import com.nqt.social.infrastructure.persistence.entity.CommentEntity;
import com.nqt.social.infrastructure.persistence.repository.CommentJpaRepository;
import com.nqt.social.usecase.boundary.CommentRepositoryGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CommentRepositoryJpaAdapter implements CommentRepositoryGateway {

    private final CommentJpaRepository jpaRepository;

    public CommentRepositoryJpaAdapter(CommentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Comment save(Comment comment) {
        CommentEntity entity = toEntity(comment);
        CommentEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId) {
        return jpaRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByPostId(Long postId) {
        return jpaRepository.countByPostId(postId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private Comment toDomain(CommentEntity e) {
        return Comment.builder()
                .id(e.getId())
                .postId(e.getPostId())
                .userId(e.getUserId())
                .parentId(e.getParentId())
                .content(e.getContent())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private CommentEntity toEntity(Comment d) {
        return CommentEntity.builder()
                .id(d.getId())
                .postId(d.getPostId())
                .userId(d.getUserId())
                .parentId(d.getParentId())
                .content(d.getContent())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
