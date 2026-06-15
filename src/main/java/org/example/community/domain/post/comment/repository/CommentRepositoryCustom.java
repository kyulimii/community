package org.example.community.domain.post.comment.repository;

import java.util.List;
import org.example.community.domain.post.comment.Comment;
import org.example.community.global.page.CursorInfo;

public interface CommentRepositoryCustom {
    List<Comment> findCommentsWithCursor(Long postId, String sort, CursorInfo cursorInfo, int limit);
}
