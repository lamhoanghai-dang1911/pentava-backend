package com.nqt.social.interfaceadapters.controller;

import com.nqt.social.infrastructure.security.CurrentUserProvider;
import com.nqt.social.usecase.boundary.InteractionInputPort;
import com.nqt.social.usecase.dto.AddCommentRequestDTO;
import com.nqt.social.usecase.dto.CommentResponseDTO;
import com.nqt.social.usecase.dto.HighFiveToggleResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Social - Interactions", description = "Tương tác High-Five và Bình luận realtime")
@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionInputPort interactionInputPort;
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "Thả hoặc huỷ thả High-Five cho bài viết")
    @PostMapping("/posts/{id}/high-five")
    public ResponseEntity<HighFiveToggleResponseDTO> toggleHighFive(@PathVariable("id") Long postId) {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(interactionInputPort.toggleHighFive(postId, userId));
    }

    @Operation(summary = "Thêm bình luận hoặc trả lời bình luận")
    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<CommentResponseDTO> addComment(
            @PathVariable("id") Long postId,
            @Valid @RequestBody AddCommentRequestDTO request) {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interactionInputPort.addComment(postId, userId, request));
    }

    @Operation(summary = "Xem danh sách bình luận của bài viết")
    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<List<CommentResponseDTO>> getComments(@PathVariable("id") Long postId) {
        Long viewerId = currentUserProvider.getOptionalCurrentUserId().orElse(null);
        return ResponseEntity.ok(interactionInputPort.getComments(postId, viewerId));
    }

    @Operation(summary = "Xoá bình luận")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("commentId") Long commentId) {
        Long userId = currentUserProvider.resolveCurrentUserId();
        interactionInputPort.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
