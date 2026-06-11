package org.example.community.domain.post.comment.api.controller;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.post.comment.api.dto.request.CommentRequest;
import org.example.community.domain.post.comment.api.dto.response.CommentPageResponse;
import org.example.community.domain.post.comment.application.CommentService;
import org.example.community.global.resolver.LoginUser;
import org.example.community.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createComment(@LoginUser Long loginUserId,
                                                           @PathVariable Long postId,
                                                           @RequestBody @Valid CommentRequest commentRequest) {
        Long id = commentService.createComment(loginUserId, postId, commentRequest);
        return ResponseEntity.created(URI.create("/comments/" + id))
                .body(null);
    }

    // 댓글 조회
    @GetMapping
    public ResponseEntity<ApiResponse<CommentPageResponse>> getComments(@PathVariable Long postId,
                                                                        @RequestParam(defaultValue = "latest") String sort,
                                                                        @RequestParam(required = false) String cursor,
                                                                        @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity
                .ok(ApiResponse.ok(commentService.getComments(postId, sort, cursor, limit)));
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> updateComment(@LoginUser Long loginUserId,
                                                           @PathVariable Long postId,
                                                           @PathVariable Long commentId,
                                                           @RequestBody @Valid CommentRequest commentRequest) {
        commentService.updateComment(loginUserId, postId, commentId, commentRequest);
        return ResponseEntity
                .ok(ApiResponse.ok(null));
    }


    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@LoginUser Long loginUserId,
                                                           @PathVariable Long postId,
                                                           @PathVariable Long commentId) {
        commentService.deleteComment(loginUserId, postId, commentId);
        return ResponseEntity
                .ok(ApiResponse.ok(null));
    }
}
