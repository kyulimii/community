package org.example.community.global.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    // JWT 인증을 건너뛸 URL
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // POST /users (회원가입) 만
        if (uri.equals("/users") && method.equals("POST")) {
            return true;
        }
        if (uri.startsWith("/users/email/") && method.equals("GET")) {
            return true;
        }
        if (uri.startsWith("/users/nickname/") && method.equals("GET")) {
            return true;
        }
        // /auth (로그인, 로그아웃)
        if (uri.equals("/auth")) {
            return true;
        }
        // 토큰 재발급 허용
        if (uri.equals("/auth/refresh") && method.equals("POST")) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws IOException, ServletException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // 토큰 서명 + 만료 검증
            // parseToken 1번만 호출 — Claims 재사용
            Claims claims = jwtProvider.parseToken(token);

            // access 토큰인지 확인
            // claims에서 직접 타입 확인 — isAccessToken() 호출 제거
            if (!"access".equals(claims.get("typ", String.class))) {
                throw new IllegalArgumentException("Not access token");
            }

            // 사용자 정보 저장
            // claims에서 직접 userId 추출 — getUserId() 호출 제거
            Long userId = Long.valueOf(claims.getSubject());
            request.setAttribute("userId", userId);
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
