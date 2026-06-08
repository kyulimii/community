package org.example.community.domain.post.comment.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentPageResponse {
    private List<CommentDetailResponse> comments;
    private String nextCursor;   // 다음 요청에 사용할 cursor
    private boolean hasNext;     // 다음 페이지 존재 여부
}
