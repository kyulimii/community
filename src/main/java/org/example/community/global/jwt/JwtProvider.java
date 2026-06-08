package org.example.community.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.example.community.global.config.JwtProperties;
import org.example.community.global.exception.CustomException;
import org.example.community.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    // JWT 생성
    private String createToken(String type, Long userId,
                               Map<String, Object> claims, long expSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(claims)
                .claim("typ", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expSeconds)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // 액세스 토큰 생성
    public String createAccessToken(Long userId, String email, String nickname) {
        return createToken("access", userId,
                Map.of("email", email, "nickname", nickname),
                jwtProperties.getAccessTokenExpSeconds());
    }

    public String createRefreshToken(Long userId) {
        return createToken("refresh", userId, Map.of(),
                jwtProperties.getRefreshTokenExpSeconds());
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);

        } catch (SignatureException | MalformedJwtException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }

    public void validateTokenType(String token, String expectedType) {
        String type = parseToken(token).get("typ", String.class);
        if (!expectedType.equals(type)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }

    public Long getUserIdFromExpiredToken(String token) {
        try {
            return Long.valueOf(Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject());
        } catch (ExpiredJwtException e) {
            // 만료 예외에서 Claims 직접 추출
            return Long.valueOf(e.getClaims().getSubject());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }

    public long getAccessTokenValidityInMilliseconds() {
        return jwtProperties.getAccessTokenExpSeconds() * 1000;
    }
}