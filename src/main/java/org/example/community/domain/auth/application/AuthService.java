package org.example.community.domain.auth.application;

import io.jsonwebtoken.Claims;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.auth.RefreshToken;
import org.example.community.domain.auth.api.dto.response.AuthResponse;
import org.example.community.domain.auth.repository.RefreshTokenRepository;
import org.example.community.domain.user.User;
import org.example.community.domain.user.api.dto.response.UserInfoResponse;
import org.example.community.domain.user.repository.UserRepository;
import org.example.community.global.config.JwtProperties;
import org.example.community.global.exception.CustomException;
import org.example.community.global.exception.ErrorCode;
import org.example.community.global.jwt.JwtProvider;
import org.example.community.global.jwt.TokenInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    // 로그인
    @Transactional
    public LoginResult login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        if (!user.matchPassword(password)) {
            throw new CustomException(ErrorCode.MISMATCH_LOGIN);
        }

        String accessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );

        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .token(refreshToken)
                        .userId(user.getId())
                        .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpSeconds()))
                        .build()
        );

        return new LoginResult(
                AuthResponse.of(
                        UserInfoResponse.from(user),
                        accessToken,
                        jwtProvider.getAccessTokenValidityInMilliseconds()
                ),
                refreshToken
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        Long userId;
        try {
            userId = jwtProvider.getUserId(refreshToken);
        } catch (CustomException e) {
            if (e.getErrorCode() == ErrorCode.TOKEN_EXPIRED) {
                userId = jwtProvider.getUserIdFromExpiredToken(refreshToken);
            } else {
                throw e;
            }
        }
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public TokenInfo refresh(String refreshToken) {
        // 1. JWT 서명 + 만료 검증
        Claims claims = jwtProvider.parseToken(refreshToken);

        // 2. refresh 타입 확인
        if (!"refresh".equals(claims.get("typ", String.class))) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 3. userId 추출
        Long userId = Long.valueOf(claims.getSubject());

        // 4. 저장된 RefreshToken 조회
        RefreshToken stored = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        // 5. 저장소 수준 만료 검사
        if (stored.isExpired()) {
            refreshTokenRepository.deleteByUserId(userId);
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 6. 토큰 일치 여부 확인 (탈취 방지)
        if (!stored.getToken().equals(refreshToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 7. 새 accessToken 발급
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        String newAccessToken = jwtProvider.createAccessToken(
                userId,
                user.getEmail(),
                user.getNickname()
        );

        return new TokenInfo(newAccessToken, jwtProvider.getAccessTokenValidityInMilliseconds());
    }
}
