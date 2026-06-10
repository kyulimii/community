package org.example.community.domain.post.api.dto.response;

import java.util.List;

public record PostPageResponse(
        List<PostListResponse> posts,
        String nextCursor,   // 다음 요청에 사용할 cursor
        boolean hasNext      // 다음 페이지 존재 여부
) {
    public static PostPageResponse of(List<PostListResponse> posts, String nextCursor, boolean hasNext) {
        return new PostPageResponse(posts, nextCursor, hasNext);
    }
}