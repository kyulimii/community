package org.example.community.domain.image.repository;

import java.util.Optional;
import org.example.community.domain.image.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
    Optional<ProfileImage> findByJpgPath(String jpgPath);
}
