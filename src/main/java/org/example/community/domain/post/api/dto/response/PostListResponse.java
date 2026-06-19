package org.example.community.domain.post.api.dto.response;

import java.time.LocalDateTime;
import org.example.community.domain.post.Post;
import org.example.community.domain.post.postStatus.PostStatus;

public record PostListResponse(
        Long id,
        String title,
        AuthorInfo author,
        int likeCount,
        int viewCount,
        int commentCount,
        LocalDateTime createdAt
) {

    public record AuthorInfo(Long id, String nickname, String profileImageUrl) {}

    public static PostListResponse of(Post post, PostStatus postStatus) {
        return new PostListResponse(
                post.getId(),
                post.getTitle(),
                new AuthorInfo(
                        post.getUser().getId(),
                        post.getUser().getNickname(),
                        post.getUser().getProfileImage()
                ),
                postStatus.getLikeCount(),
                postStatus.getViewCount(),
                postStatus.getCommentCount(),
                post.getCreatedAt()
        );
    }
}