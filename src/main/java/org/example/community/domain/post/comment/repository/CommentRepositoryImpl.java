package org.example.community.domain.post.comment.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.post.comment.Comment;
import org.example.community.domain.post.comment.QComment;
import org.example.community.domain.user.QUser;
import org.example.community.global.page.CursorInfo;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Comment> findCommentsWithCursor(Long postId, String sort, CursorInfo cursorInfo, int limit) {
        QComment qComment = QComment.comment;
        QUser qUser = QUser.user;

        return queryFactory
                .selectFrom(qComment)
                // fetchJoin: Comment 조회 시 User를 한 번에 가져옴 → N+1 방지
                .join(qComment.user, qUser).fetchJoin()
                .where(
                        qComment.post.id.eq(postId),
                        buildCursorCondition(qComment, sort, cursorInfo)
                )
                .orderBy(buildOrderBy(qComment, sort))
                .limit(limit)
                .fetch();
    }

    private BooleanExpression buildCursorCondition(QComment qComment, String sort, CursorInfo cursorInfo) {
        if (cursorInfo == null) {
            return null;
        }

        return switch (sort) {
            case "latest" -> qComment.createdAt.lt(cursorInfo.getCreatedAt()) // createdAt < cursor
                    .or(qComment.createdAt.eq(cursorInfo.getCreatedAt())        // OR (createdAt = cursor
                            .and(qComment.id.lt(cursorInfo.getId())));          //     AND id < cursor.id)
            case "oldest" -> qComment.createdAt.gt(cursorInfo.getCreatedAt()) // createdAt > cursor
                    .or(qComment.createdAt.eq(cursorInfo.getCreatedAt())        // OR (createdAt = cursor
                            .and(qComment.id.gt(cursorInfo.getId())));          //     AND id > cursor.id)
            default -> null;
        };
    }

    private OrderSpecifier<?>[] buildOrderBy(QComment qComment, String sort) {
        return switch (sort) {
            case "latest" -> new OrderSpecifier[]{qComment.createdAt.desc(), qComment.id.desc()};
            case "oldest" -> new OrderSpecifier[]{qComment.createdAt.asc(), qComment.id.asc()};
            default -> new OrderSpecifier[]{qComment.createdAt.asc()};
        };
    }
}
