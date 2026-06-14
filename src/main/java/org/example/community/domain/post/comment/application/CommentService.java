package org.example.community.domain.post.comment.application;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.post.Post;
import org.example.community.domain.post.comment.Comment;
import org.example.community.domain.post.comment.api.dto.request.CommentRequest;
import org.example.community.domain.post.comment.api.dto.response.CommentDetailResponse;
import org.example.community.domain.post.comment.api.dto.response.CommentPageResponse;
import org.example.community.domain.post.comment.repository.CommentRepository;
import org.example.community.domain.post.postStatus.PostStatus;
import org.example.community.domain.post.postStatus.repository.PostStatusRepository;
import org.example.community.domain.post.repository.PostRepository;
import org.example.community.domain.user.User;
import org.example.community.domain.user.repository.UserRepository;
import org.example.community.global.exception.CustomException;
import org.example.community.global.exception.ErrorCode;
import org.example.community.global.page.CursorInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostStatusRepository postStatusRepository;

    // 댓글 작성
    @Transactional
    public Long createComment(Long userId, Long postId, CommentRequest commentRequest) {
        User user = findUserById(userId);
        Post post = findPostById(postId);
        PostStatus postStatus = findPostStatusByPostId(postId);

        Comment comment = Comment.builder()
                .content(commentRequest.content())
                .user(user)
                .post(post)
                .build();

        commentRepository.save(comment);
        postStatus.increaseCommentCount();

        postStatusRepository.save(postStatus);

        return comment.getId();
    }

    // 댓글 조회
    public CommentPageResponse getComments(Long postId, String sort, String cursor, int limit) {
        if (!sort.equals("latest") && !sort.equals("oldest")) {
            throw new CustomException(ErrorCode.INVALID_SORT);
        }

        CursorInfo cursorInfo = CursorInfo.from(cursor, sort);

        List<Comment> comments = commentRepository.findCommentsWithCursor(postId, sort, cursorInfo, limit + 1);

        boolean hasNext = comments.size() > limit;
        List<Comment> result = hasNext ? comments.subList(0, limit) : comments;

        String nextCursor = hasNext
                ? CursorInfo.encode(
                result.get(result.size() - 1).getCreatedAt(),
                result.get(result.size() - 1).getId())
                : null;

        return CommentPageResponse.of(
                result.stream()
                        .map(comment -> CommentDetailResponse.from(
                                comment,
                                // fetchJoin으로 이미 로딩된 상태 — 추가 쿼리 없음
                                comment.getUser() != null
                                        ? comment.getUser().getNickname()
                                        : "탈퇴한 사용자"
                        ))
                        .toList(),
                nextCursor,
                hasNext
        );
    }

    // 댓글 수정
    @Transactional
    public void updateComment(Long userId, Long postId, Long commentId, CommentRequest commentRequest) {
        Comment comment = findAndValidate(userId, postId, commentId);

        comment.update(commentRequest.content());
        commentRepository.save(comment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long userId, Long postId, Long commentId) {
        findAndValidate(userId, postId, commentId);
        PostStatus postStatus = findPostStatusByPostId(postId);
        commentRepository.deleteById(commentId);
        postStatus.decreaseCommentCount();
        postStatusRepository.save(postStatus);
    }

    // 댓글 조회 + 검증
    private Comment findAndValidate(Long userId, Long postId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COMMENT));

        if (!Objects.equals(comment.getPost().getId(), postId)) {
            throw new CustomException(ErrorCode.NOT_FOUND_COMMENT);
        }

        if (!Objects.equals(comment.getUser().getId(), userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return comment;
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    }

    private PostStatus findPostStatusByPostId(Long postId) {
        return postStatusRepository.findPostStatusByPostId(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
    }

}