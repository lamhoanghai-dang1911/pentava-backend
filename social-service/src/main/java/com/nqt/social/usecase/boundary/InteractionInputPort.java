package com.nqt.social.usecase.boundary;

import com.nqt.social.usecase.dto.AddCommentRequestDTO;
import com.nqt.social.usecase.dto.CommentResponseDTO;
import com.nqt.social.usecase.dto.HighFiveToggleResponseDTO;

import java.util.List;

public interface InteractionInputPort {
    HighFiveToggleResponseDTO toggleHighFive(Long postId, Long userId);
    CommentResponseDTO addComment(Long postId, Long userId, AddCommentRequestDTO request);
    List<CommentResponseDTO> getComments(Long postId, Long viewerId);
    void deleteComment(Long commentId, Long userId);
}
