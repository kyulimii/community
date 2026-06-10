package org.example.community.domain.post.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.post.api.dto.request.PostRequest;
import org.example.community.domain.post.api.dto.response.PostCreateResponse;
import org.example.community.domain.post.api.dto.response.PostDetailResponse;
import org.example.community.domain.post.api.dto.response.PostPageResponse;
import org.example.community.domain.post.application.PostService;
import org.example.community.global.resolver.LoginUser;
import org.example.community.global.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostCreateResponse> createPost(
            @LoginUser Long loginUserId,
            @RequestPart("post") @Valid PostRequest postRequest,
            @RequestPart(value = "postImage", required = false) MultipartFile postImage
    ) {
        return ApiResponse.created(postService.createPost(loginUserId, postRequest, postImage));
    }

    // 게시글 목록 조회
    @GetMapping
    public ApiResponse<PostPageResponse> getPosts(
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String cursor, // 최초 요청은 없어도 O
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ApiResponse.ok(postService.getPosts(sort, cursor, limit));
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public ApiResponse<PostDetailResponse> getPost(@PathVariable Long postId) {
        return ApiResponse.ok(postService.getPost(postId));
    }

    // 게시글 수정
    @PatchMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> updatePost(
            @LoginUser Long loginUserId,
            @PathVariable Long postId,
            @RequestPart(value = "post", required = false) @Valid PostRequest postRequest,
            @RequestPart(value = "postImage", required = false) MultipartFile postImage
    ) {
        postService.updatePost(loginUserId, postId, postRequest, postImage);
        return ApiResponse.ok(null);
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(
            @LoginUser Long loginUserId,
            @PathVariable Long postId
    ) {
        postService.deletePost(loginUserId, postId);
        return ApiResponse.ok(null);
    }

    // 좋아요 등록
    @PostMapping("/{postId}/likes")
    public ApiResponse<Void> createLike(
            @LoginUser Long loginUserId,
            @PathVariable Long postId
    ) {
        postService.createLike(loginUserId, postId);
        return ApiResponse.ok(null);
    }

    // 좋아요 취소
    @DeleteMapping("/{postId}/likes")
    public ApiResponse<Void> deleteLike(
            @LoginUser Long loginUserId,
            @PathVariable Long postId
    ) {
        postService.deleteLike(loginUserId, postId);
        return ApiResponse.ok(null);
    }
}