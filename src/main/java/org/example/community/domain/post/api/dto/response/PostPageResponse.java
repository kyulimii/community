package org.example.community.domain.post.api.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PostPageResponse {
    private List<PostListResponse> posts;
    private String nextCursor;   // 다음 요청에 사용할 cursor
    private boolean hasNext;     // 다음 페이지 존재 여부
}