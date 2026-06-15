package org.example.community.domain.post.repository;

import java.util.List;
import org.example.community.domain.post.api.dto.response.PostWithStatus;
import org.example.community.global.page.CursorInfo;

public interface PostRepositoryCustom {
    List<PostWithStatus> findPostsWithCursor(String sort, CursorInfo cursorInfo, int limit);
}
