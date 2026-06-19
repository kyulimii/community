package org.example.community.domain.post.api.dto.response;

import java.time.LocalDateTime;
import org.example.community.domain.post.Post;
import org.example.community.domain.post.postStatus.PostStatus;
import org.example.community.domain.user.User;

public record PostDetailResponse(
        Long id,
        String title,
        String content,
        String fileUrl,
        Long userId,
        String nickname,
        String profileImage,
        boolean isLiked,
        int likeCount,
        int viewCount,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PostDetailResponse of(Post post, User user, PostStatus postStatus, boolean isLike) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getPostImage(),
                user.getId(),
                user.getNickname(),
                user.getProfileImage(),
                isLike,
                postStatus.getLikeCount(),
                postStatus.getViewCount(),
                postStatus.getCommentCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}