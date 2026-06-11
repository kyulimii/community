package org.example.community.domain.post.comment.api.dto.response;

import java.util.List;

public record CommentPageResponse(
        List<CommentDetailResponse> comments,
        String nextCursor,   // 다음 요청에 사용할 cursor
        boolean hasNext      // 다음 페이지 존재 여부
) {
    public static CommentPageResponse of(List<CommentDetailResponse> comments, String nextCursor, boolean hasNext) {
        return new CommentPageResponse(comments, nextCursor, hasNext);
    }
}
