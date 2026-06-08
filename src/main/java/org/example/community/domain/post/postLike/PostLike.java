package org.example.community.domain.post.postLike;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.community.domain.post.Post;
import org.example.community.domain.user.User;

@Entity
@Getter
@IdClass(PostLikeId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike {
    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "post_id", nullable = false)
    private Long postId;

    // FK 관계 — @Id와 별도로 연관관계 매핑
    // insertable/updatable = false: userId, postId 컬럼을 @Id가 이미 관리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;

    @Builder
    private PostLike(Long userId, Long postId) {
        this.userId = userId;
        this.postId = postId;
    }
}
