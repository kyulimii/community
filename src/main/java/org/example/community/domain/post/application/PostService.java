package org.example.community.domain.post.application;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.image.PostImage;
import org.example.community.domain.image.api.dto.response.PostCreateResponse;
import org.example.community.domain.image.application.PostImageService;
import org.example.community.domain.image.repository.PostImageRepository;
import org.example.community.domain.post.Post;
import org.example.community.domain.post.api.dto.request.PostRequest;
import org.example.community.domain.post.api.dto.response.PostDetailResponse;
import org.example.community.domain.post.api.dto.response.PostListResponse;
import org.example.community.domain.post.api.dto.response.PostPageResponse;
import org.example.community.domain.post.api.dto.response.PostWithStatus;
import org.example.community.domain.post.comment.repository.CommentRepository;
import org.example.community.domain.post.postLike.PostLike;
import org.example.community.domain.post.postLike.repository.PostLikeRepository;
import org.example.community.domain.post.postStatus.PostStatus;
import org.example.community.domain.post.postStatus.ViewCountBuffer;
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
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final PostStatusRepository postStatusRepository;
    private final ViewCountBuffer viewCountBuffer;
    private final PostImageRepository postImageRepository;
    private final PostImageService imageService;

    // 게시글 작성
    @Transactional
    public PostCreateResponse createPost(Long userId, PostRequest postRequest) {
        User user = findUserById(userId);

//        PostImage postImage = postImageRepository.findByJpgPath(postRequest.postImageUrl())
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_IMAGE));
        PostImage postImage = postImageRepository.findByJpgPath(postRequest.postImageUrl())
                .orElse(null);

        Post post = Post.builder()
                .title(postRequest.title())
                .content(postRequest.content())
                .postImage(postRequest.postImageUrl())
                .user(user)
                .build();
        postRepository.save(post);
        if (postImage != null)
            postImage.assignToPost(post);

        PostStatus postStatus = PostStatus.builder()
                .post(post)
                .build();
        postStatusRepository.save(postStatus);

        return new PostCreateResponse(post.getId());
    }

    // 최초 요청: GET /posts?sort=latest&limit=10
    // 이후 요청: GET /posts?sort=latest&cursor=2026-05-31T12:00:00Z|1&limit=10
    // 게시글 목록 조회
    public PostPageResponse getPosts(String sort, String cursor, int limit) {

        CursorInfo cursorInfo = CursorInfo.from(cursor, sort);
        List<PostWithStatus> posts = postRepository.findPostsWithCursor(sort, cursorInfo, limit + 1);

        boolean hasNext = posts.size() > limit;
        List<PostWithStatus> result = hasNext ? posts.subList(0, limit) : posts;

        String nextCursor = hasNext
                ? sort.equals("popular")
                ? CursorInfo.encode(
                result.get(result.size() - 1).postStatus().getLikeCount(),
                result.get(result.size() - 1).post().getId())
                : CursorInfo.encode(
                        result.get(result.size() - 1).post().getCreatedAt(),
                        result.get(result.size() - 1).post().getId())
                : null;

        return PostPageResponse.of(
                result.stream()
                        .map(pw -> PostListResponse.of(pw.post(), pw.postStatus()))
                        .toList(),
                nextCursor,
                hasNext
        );
    }

    // 게시글 상세 조회
    public PostDetailResponse getPostDetail(Long userId, Long postId) {
        Post post = findPostById(postId);
        PostStatus postStatus = findPostStatusByPostId(postId);
        boolean isLike = postLikeRepository.existsByUserIdAndPostId(userId, postId);

        viewCountBuffer.increment(postId);

        return PostDetailResponse.of(post, post.getUser(), postStatus, isLike);
    }

    // 게시글 수정
    @Transactional
    public void updatePost(Long userId, Long postId, PostRequest postRequest) {
        Post post = findPostById(postId);

        if (!Objects.equals(post.getUser().getId(), userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        String oldImagePath = post.getPostImage();
        String newImageUrl = postRequest.postImageUrl();

        if (newImageUrl != null && !newImageUrl.equals(oldImagePath)) {
            postImageRepository.findByJpgPath(newImageUrl)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_IMAGE));
            if (oldImagePath != null) {
                imageService.deleteImage(oldImagePath);
            }
        }

        post.update(postRequest.title(), postRequest.content(),
                newImageUrl != null ? newImageUrl : oldImagePath);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = findPostById(postId);

        if (!Objects.equals(post.getUser().getId(), userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        commentRepository.deleteByPostId(postId);
        postLikeRepository.deleteByPostId(postId);
        postStatusRepository.deleteByPostId(postId);
        imageService.deleteImage(post.getPostImage());

        postRepository.deleteById(postId);
    }

    // 좋아요 등록
    @Transactional
    public void createLike(Long userId, Long postId) {
        Post post = findPostById(postId);
        PostStatus postStatus = findPostStatusByPostId(postId);

        if (postLikeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        }

        postLikeRepository.save(PostLike.builder()
                .userId(userId)
                .postId(postId)
                .build());
        postStatus.increaseLikeCount();
        postStatusRepository.save(postStatus);
    }

    // 좋아요 취소
    @Transactional
    public void deleteLike(Long userId, Long postId) {
        Post post = findPostById(postId);
        PostStatus postStatus = findPostStatusByPostId(postId);

        if (!postLikeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new CustomException(ErrorCode.NOT_LIKED);
        }
        postLikeRepository.deleteByUserIdAndPostId(userId, postId);
        postStatus.decreaseLikeCount();
        postStatusRepository.save(postStatus);
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
