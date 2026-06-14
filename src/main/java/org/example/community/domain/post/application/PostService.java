package org.example.community.domain.post.application;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.image.Image;
import org.example.community.domain.image.application.ImageService;
import org.example.community.domain.image.repository.ImageRepository;
import org.example.community.domain.post.Post;
import org.example.community.domain.post.api.dto.request.PostRequest;
import org.example.community.domain.post.api.dto.response.PostDetailResponse;
import org.example.community.domain.post.api.dto.response.PostListResponse;
import org.example.community.domain.post.api.dto.response.PostPageResponse;
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
    private final ImageRepository imageRepository;
    private final ImageService imageService;

    // 게시글 작성
    @Transactional
    public Long createPost(Long userId, PostRequest postRequest) {
        User user = findUserById(userId);

        Image image = imageRepository.findById(postRequest.imageId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_IMAGE));

        image.updateImageType("POST");

        Post post = Post.builder()
                .title(postRequest.title())
                .content(postRequest.content())
                .postImage(image.getJpgPath())
                .user(user)
                .build();
        postRepository.save(post);

        PostStatus postStatus = PostStatus.builder()
                .post(post)
                .build();
        postStatusRepository.save(postStatus);

        return post.getId();
    }

    // 최초 요청: GET /posts?sort=latest&limit=10
    // 이후 요청: GET /posts?sort=latest&cursor=2026-05-31T12:00:00Z|1&limit=10
    // 게시글 목록 조회
    public PostPageResponse getPosts(String sort, String cursor, int limit) {

        // cursor 파싱 (최초 요청이면 null)
        CursorInfo cursorInfo = CursorInfo.from(cursor, sort);

        // 정렬 기준
        Comparator<Post> comparator = switch (sort) {
            case "latest" -> Comparator.comparing(Post::getCreatedAt).reversed()
                    .thenComparing(Comparator.comparing(Post::getId).reversed());
            case "oldest" -> Comparator.comparing(Post::getCreatedAt)
                    .thenComparing(Post::getId);
            case "popular" -> Comparator.comparingInt(
                            (Post post) -> findPostStatusByPostId(post.getId()).getLikeCount())
                    .reversed()
                    .thenComparing(Comparator.comparing(Post::getId).reversed());
            default -> throw new CustomException(ErrorCode.INVALID_SORT);
        };

        // 전체 조회 → 정렬 → cursor 이후 필터 → limit+1개 조회
        List<Post> posts = postRepository.findAll()
                .stream()
                .sorted(comparator)
                .filter(post -> isAfterCursor(post, cursorInfo, sort))
                .limit(limit + 1) // hasNext 여부 확인 위해 + 1
                .toList();

        // 다음 페이지 존재 여부 확인
        boolean hasNext = posts.size() > limit;
        List<Post> result = hasNext ? posts.subList(0, limit) : posts;

        // 다음 cursor 생성
        String nextCursor = hasNext
                ? sort.equals("popular")
                ? CursorInfo.encode(
                findPostStatusByPostId(result.get(result.size() - 1).getId()).getLikeCount(),
                result.get(result.size() - 1).getId())
                : CursorInfo.encode(result.get(result.size() - 1).getCreatedAt(), result.get(result.size() - 1).getId())
                : null;

        return PostPageResponse.of(
                result.stream()
                        .map(post -> PostListResponse.of(post, findPostStatusByPostId(post.getId())))
                        .toList(),
                nextCursor,
                hasNext
        );
    }

    // 게시글 상세 조회
    public PostDetailResponse getPost(Long postId) {
        Post post = findPostById(postId);
        PostStatus postStatus = findPostStatusByPostId(postId);

        viewCountBuffer.increment(postId);

        return PostDetailResponse.of(post, postStatus);
    }

    // 게시글 수정
    @Transactional
    public void updatePost(Long userId, Long postId, PostRequest postRequest) {
        Post post = findPostById(postId);

        if (!Objects.equals(post.getUser().getId(), userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        Image image = imageRepository.findById(postRequest.imageId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_IMAGE));

        if (image.getImageType() != null) {
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
        post.update(postRequest.title(), postRequest.content(), image.getJpgPath());
        image.updateImageType("POST");

        postRepository.save(post);
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

    // 커서 이후 데이터 있는지 확인
    private boolean isAfterCursor(Post post, CursorInfo cursorInfo, String sort) {
        if (cursorInfo == null) {
            return true;
        }
        PostStatus postStatus = findPostStatusByPostId(post.getId());

        return switch (sort) {
            case "latest" -> post.getCreatedAt().isBefore(cursorInfo.getCreatedAt()) ||
                    (post.getCreatedAt().isEqual(cursorInfo.getCreatedAt()) &&
                            post.getId() < cursorInfo.getId());
            case "oldest" -> post.getCreatedAt().isAfter(cursorInfo.getCreatedAt()) ||
                    (post.getCreatedAt().isEqual(cursorInfo.getCreatedAt()) &&
                            post.getId() > cursorInfo.getId());
            case "popular" -> postStatus.getLikeCount() < cursorInfo.getLikeCount() ||
                    (postStatus.getLikeCount() == cursorInfo.getLikeCount() &&
                            post.getId() < cursorInfo.getId());
            default -> throw new CustomException(ErrorCode.INVALID_SORT);
        };
    }
}