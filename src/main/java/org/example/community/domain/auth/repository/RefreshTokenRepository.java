package org.example.community.domain.auth.repository;

import java.util.Optional;
import org.example.community.domain.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}