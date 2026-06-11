package org.example.community.domain.post.api.dto.response;

import java.time.LocalDateTime;
import org.example.community.domain.post.Post;
import org.example.community.domain.post.postStatus.PostStatus;

public record PostListResponse(
        // content, postImage 제외
        Long id,
        String title,
        Long authorId,
        int likeCount,
        int viewCount,
        int commentCount,
        LocalDateTime createdAt
) {

    public static PostListResponse of(Post post, PostStatus postStatus) {
        return new PostListResponse(
                post.getId(),
                post.getTitle(),
                post.getUser().getId(),
                postStatus.getLikeCount(),
                postStatus.getViewCount(),
                postStatus.getCommentCount(),
                post.getCreatedAt()
        );
    }
}