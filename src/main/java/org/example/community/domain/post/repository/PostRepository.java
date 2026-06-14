package org.example.community.domain.post.repository;

import org.example.community.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    void deleteByUserId(Long loginUserId);
}
