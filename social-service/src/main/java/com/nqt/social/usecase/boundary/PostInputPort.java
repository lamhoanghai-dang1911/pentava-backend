package com.nqt.social.usecase.boundary;

import com.nqt.social.usecase.dto.CreatePostRequestDTO;
import com.nqt.social.usecase.dto.PostResponseDTO;
import com.nqt.social.usecase.dto.UpdatePostRequestDTO;

import java.util.List;

public interface PostInputPort {
    PostResponseDTO createPost(Long userId, CreatePostRequestDTO request);
    PostResponseDTO updatePost(Long postId, Long userId, UpdatePostRequestDTO request);
    PostResponseDTO getPost(Long postId, Long viewerId);
    void deletePost(Long postId, Long userId);
    List<PostResponseDTO> getUserPosts(Long targetUserId, Long viewerId);
}
