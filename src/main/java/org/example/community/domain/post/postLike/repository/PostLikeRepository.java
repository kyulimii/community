package org.example.community.domain.post.postLike.repository;

import org.example.community.domain.post.postLike.PostLike;
import org.example.community.domain.post.postLike.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    void deleteByUserIdAndPostId(Long userId, Long postId);
    void deleteByPostId(Long postId); // 게시글 삭제 시 연관 좋아요 전체 삭제
    void deleteByUserId(Long userId); // 회원 탈퇴 시 연관 좋아요 전체 삭제
}