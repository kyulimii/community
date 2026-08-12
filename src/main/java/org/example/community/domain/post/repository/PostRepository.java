package org.example.community.domain.post.repository;

import java.util.List;
import org.example.community.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    List<Post> findByUserId(Long loginUserId);
    void deleteByUserId(Long loginUserId);
}
