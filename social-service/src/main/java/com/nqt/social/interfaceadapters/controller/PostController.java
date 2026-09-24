package com.nqt.social.interfaceadapters.controller;

import com.nqt.social.infrastructure.security.CurrentUserProvider;
import com.nqt.social.usecase.boundary.PostInputPort;
import com.nqt.social.usecase.dto.CreatePostRequestDTO;
import com.nqt.social.usecase.dto.PostResponseDTO;
import com.nqt.social.usecase.dto.UpdatePostRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Social - Posts", description = "Quản lý bài đăng clip mạng xã hội")
@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class PostController {

    private final PostInputPort postInputPort;
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "Đăng clip từ cinema-service lên mạng xã hội")
    @PostMapping("/posts")
    public ResponseEntity<PostResponseDTO> createPost(@Valid @RequestBody CreatePostRequestDTO request) {
        Long userId = currentUserProvider.resolveCurrentUserId();
        PostResponseDTO post = postInputPort.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @Operation(summary = "Chỉnh sửa bài viết của chính mình (caption, tags, privacyMode)")
    @PutMapping("/posts/{id}")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdatePostRequestDTO request) {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(postInputPort.updatePost(id, userId, request));
    }

    @Operation(summary = "Xem chi tiết một bài viết")
    @GetMapping("/posts/{id}")
    public ResponseEntity<PostResponseDTO> getPost(@PathVariable("id") Long id) {
        Long viewerId = currentUserProvider.getOptionalCurrentUserId().orElse(null);
        return ResponseEntity.ok(postInputPort.getPost(id, viewerId));
    }

    @Operation(summary = "Xoá bài viết của chính mình")
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") Long id) {
        Long userId = currentUserProvider.resolveCurrentUserId();
        postInputPort.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Xem danh sách bài viết của một người dùng")
    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<List<PostResponseDTO>> getUserPosts(@PathVariable("userId") Long userId) {
        Long viewerId = currentUserProvider.getOptionalCurrentUserId().orElse(null);
        return ResponseEntity.ok(postInputPort.getUserPosts(userId, viewerId));
    }
}
