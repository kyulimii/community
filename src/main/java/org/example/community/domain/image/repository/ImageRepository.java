package org.example.community.domain.image.repository;

import java.util.Optional;
import org.example.community.domain.image.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByJpgPath(String jpgPath);
}
