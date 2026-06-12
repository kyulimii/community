package org.example.community.domain.post.api.controller;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.post.api.dto.request.PostRequest;
import org.example.community.domain.post.api.dto.response.PostDetailResponse;
import org.example.community.domain.post.api.dto.response.PostPageResponse;
import org.example.community.domain.post.application.PostService;
import org.example.community.global.resolver.LoginUser;
import org.example.community.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createPost(
            @LoginUser Long loginUserId,
            @RequestBody @Valid PostRequest postRequest
    ) {
        Long id = postService.createPost(loginUserId, postRequest);
        return ResponseEntity
                .created(URI.create("/posts/" + id))
                .body(ApiResponse.created(null));
    }

    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PostPageResponse>> getPosts(
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String cursor, // 최초 요청은 없어도 O
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity
                .ok(ApiResponse.ok(postService.getPosts(sort, cursor, limit)));
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(@PathVariable Long postId) {
        return ResponseEntity
                .ok(ApiResponse.ok(postService.getPost(postId)));
    }

    // 게시글 수정
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @LoginUser Long loginUserId,
            @PathVariable Long postId,
            @RequestBody @Valid PostRequest postRequest
    ) {
        postService.updatePost(loginUserId, postId, postRequest);
        return ResponseEntity
                .ok(ApiResponse.ok(null));
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @LoginUser Long loginUserId,
            @PathVariable Long postId
    ) {
        postService.deletePost(loginUserId, postId);
        return ResponseEntity
                .ok(ApiResponse.ok(null));
    }

    // 좋아요 등록
    @PostMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<Void>> createLike(
            @LoginUser Long loginUserId,
            @PathVariable Long postId
    ) {
        postService.createLike(loginUserId, postId);
        return ResponseEntity
                .ok(ApiResponse.ok(null));
    }

    // 좋아요 취소
    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<Void>> deleteLike(
            @LoginUser Long loginUserId,
            @PathVariable Long postId
    ) {
        postService.deleteLike(loginUserId, postId);
        return ResponseEntity
                .ok(ApiResponse.ok(null));
    }
}