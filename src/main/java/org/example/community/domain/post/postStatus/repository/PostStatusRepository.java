package org.example.community.domain.post.postStatus.repository;

import java.util.Optional;
import org.example.community.domain.post.postStatus.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PostStatusRepository extends JpaRepository<PostStatus, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE PostStatus p SET p.viewCount = p.viewCount + :count WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId, @Param("count") int count);

    Optional<PostStatus> findPostStatusByPostId(Long postId);
}
