package org.example.community.domain.post.comment.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.post.comment.api.dto.CommentPageResponse;
import org.example.community.domain.post.comment.api.dto.CommentRequestDto;
import org.example.community.domain.post.comment.application.CommentService;
import org.example.community.global.resolver.LoginUser;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Void> createComment(@LoginUser Long loginUserId,
                                              @PathVariable Long postId,
                                              @RequestBody @Valid CommentRequestDto commentRequestDto) {
        commentService.createComment(loginUserId, postId, commentRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 댓글 조회
    @GetMapping
    public ResponseEntity<CommentPageResponse> getComments(@PathVariable Long postId,
                                                           @RequestParam(defaultValue = "latest") String sort,
                                                           @RequestParam(required = false) String cursor,
                                                           @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(commentService.getComments(postId, sort, cursor, limit));
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(@LoginUser Long loginUserId,
                                              @PathVariable Long postId,
                                              @PathVariable Long commentId,
                                              @RequestBody @Valid CommentRequestDto commentRequestDto) {
        commentService.updateComment(loginUserId, postId, commentId, commentRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@LoginUser Long loginUserId,
                                              @PathVariable Long postId,
                                              @PathVariable Long commentId) {
        commentService.deleteComment(loginUserId, postId, commentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
