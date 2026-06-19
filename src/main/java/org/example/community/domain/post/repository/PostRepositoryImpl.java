package org.example.community.domain.post.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.post.Post;
import org.example.community.domain.post.QPost;
import org.example.community.domain.post.api.dto.response.PostWithStatus;
import org.example.community.domain.post.postStatus.QPostStatus;
import org.example.community.domain.user.QUser;
import org.example.community.global.page.CursorInfo;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    // cursorInfo 이전 페이지의 마지막 항목 정보. null이면 첫 페이지
    @Override
    public List<PostWithStatus> findPostsWithCursor(String sort, CursorInfo cursorInfo, int limit) {
        QPost qPost = QPost.post;
        QPostStatus qPostStatus = QPostStatus.postStatus;
        QUser qUser = QUser.user;

        return queryFactory
                .select(Projections.constructor(PostWithStatus.class, qPost, qPostStatus))
                .from(qPost)
                .join(qPost.user, qUser).fetchJoin()
                .join(qPostStatus).on(qPostStatus.post.eq(qPost))
                .where(buildCursorCondition(qPost, qPostStatus, sort, cursorInfo))
                .orderBy(buildOrderBy(qPost, qPostStatus, sort))
                .limit(limit)
                .fetch();
        /**
         * SELECT *
         * FROM post
         * WHERE created_at < '2024-01-15 12:00:00'
         *    OR (created_at = '2024-01-15 12:00:00' AND id < 42)
         * ORDER BY created_at DESC, id DESC
         * LIMIT 10
         */
    }

    // 커서 조건 만들기
    private BooleanExpression buildCursorCondition(QPost qPost, QPostStatus qPostStatus,
                                                   String sort, CursorInfo cursorInfo) {
        if (cursorInfo == null) {
            return null;
        }

        return switch (sort) {
            case "latest" -> qPost.createdAt.lt(cursorInfo.getCreatedAt()) // createdAt < cursor
                    .or(qPost.createdAt.eq(cursorInfo.getCreatedAt())        // OR (createdAt = cursor
                            .and(qPost.id.lt(cursorInfo.getId())));          //     AND id < cursor.id)
            case "oldest" -> qPost.createdAt.gt(cursorInfo.getCreatedAt()) // createdAt > cursor
                    .or(qPost.createdAt.eq(cursorInfo.getCreatedAt())        // OR (createdAt = cursor
                            .and(qPost.id.gt(cursorInfo.getId())));          //     AND id > cursor.id)
            case "popular" -> qPostStatus.likeCount.lt(cursorInfo.getLikeCount()) // likeCount < cursor
                    .or(qPostStatus.likeCount.eq(cursorInfo.getLikeCount())         // OR (likeCount = cursor
                            .and(qPost.id.lt(cursorInfo.getId())));                 //     AND id < cursor.id)
            default -> null;
        };
    }

    private OrderSpecifier<?>[] buildOrderBy(QPost qPost, QPostStatus qPostStatus, String sort) {
        return switch (sort) {
            case "latest" -> new OrderSpecifier[]{qPost.createdAt.desc(), qPost.id.desc()};
            case "oldest" -> new OrderSpecifier[]{qPost.createdAt.asc(), qPost.id.asc()};
            case "popular" -> new OrderSpecifier[]{qPostStatus.likeCount.desc(), qPost.id.desc()};
            default -> new OrderSpecifier[]{qPost.createdAt.desc()};
        };
    }
}
