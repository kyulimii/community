package org.example.community.domain.image.repository;

import java.util.Optional;
import org.example.community.domain.image.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    Optional<PostImage> findByJpgPath(String jpgPath);
}
