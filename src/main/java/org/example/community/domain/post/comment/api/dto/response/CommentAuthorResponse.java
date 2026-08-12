package org.example.community.domain.post.comment.api.dto.response;

import org.example.community.domain.user.User;

public record CommentAuthorResponse(
        Long userId,
        String nickname,
        String profileImageUrl
) {
    public static CommentAuthorResponse from(User user) {
        return new CommentAuthorResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImage()
        );
    }
}
