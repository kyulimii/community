package org.example.community.domain.post.api.dto.response;

import java.time.LocalDateTime;
import org.example.community.domain.post.Post;
import org.example.community.domain.post.postStatus.PostStatus;

public record PostDetailResponse(
        Long id,
        String title,
        String content,
        String postImage,
        Long authorId,
        int likeCount,
        int viewCount,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PostDetailResponse of(Post post, PostStatus postStatus) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getPostImage(),
                post.getUser().getId(),
                postStatus.getLikeCount(),
                postStatus.getViewCount(),
                postStatus.getCommentCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}