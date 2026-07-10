package org.example.community.global.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.community.global.exception.CustomException;
import org.example.community.global.jwt.JwtProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
@RequiredArgsConstructor
public class LoginCheckFilter extends GenericFilterBean {

    private final JwtProvider jwtProvider;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            if (!isWhiteList(httpRequest)) {
                String authHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    sendUnauthorized(httpRequest, httpResponse, "UNAUTHORIZED", "인증이 필요합니다.");
                    return;
                }

                Claims claims = jwtProvider.parseToken(authHeader.substring(7));

                if (!"access".equals(claims.get("typ", String.class))) {
                    sendUnauthorized(httpRequest, httpResponse, "INVALID_TOKEN", "유효하지 않은 토큰입니다.");
                    return;
                }

                Long userId = Long.valueOf(claims.getSubject());
                httpRequest.setAttribute("userId", userId);
                log.debug("인증 완료 userId={}", userId);
            }
        } catch (CustomException e) {
            sendUnauthorized(httpRequest, httpResponse, e.getErrorCode().name(), e.getErrorCode().getMessage());
            return;
        } catch (Exception e) {
            sendUnauthorized(httpRequest, httpResponse, "INVALID_TOKEN", "유효하지 않은 토큰입니다.");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isWhiteList(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equals(method)) return true;
        if ("/users".equals(uri) && "POST".equals(method)) return true;
        if (uri.startsWith("/users/email/") && "GET".equals(method)) return true;
        if (uri.startsWith("/users/nickname/") && "GET".equals(method)) return true;
        if (uri.startsWith("/auth") && "POST".equals(method)) return true;
        if (uri.startsWith("/uploads/") && "GET".equals(method)) return true;
        if ("/uploads".equals(uri) && "POST".equals(method)) return true;
        if ("/uploads/profile-image".equals(uri) && "POST".equals(method)) return true;
        if ("/auth/health".equals(uri) && "GET".equals(method)) return true;

        return false;
    }

    private void sendUnauthorized(HttpServletRequest request, HttpServletResponse response,
            String code, String message) throws IOException {
        String origin = request.getHeader("Origin");
        if (origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Vary", "Origin");
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                String.format("{\"isSuccess\":false,\"code\":\"%s\",\"message\":\"%s\"}", code, message)
        );
    }
}
