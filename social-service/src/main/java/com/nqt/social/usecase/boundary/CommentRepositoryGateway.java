package com.nqt.social.usecase.boundary;

import com.nqt.social.domain.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepositoryGateway {
    Comment save(Comment comment);
    Optional<Comment> findById(Long id);
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
    long countByPostId(Long postId);
    void deleteById(Long id);
}
