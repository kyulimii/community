package org.example.community.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
public class LogFilter extends GenericFilterBean {

    // 4xx, 5xx만 실패로 간주 (2xx, 3xx는 로그 안 남김)
    private static final int FAILURE_THRESHOLD = 400;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uuid = UUID.randomUUID().toString();
        String method = httpRequest.getMethod();
        String requestURI = httpRequest.getRequestURI();

        Exception thrown = null;
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            // 필터 체인 도중 예외가 터진 경우도 실패로 간주
            thrown = e;
            throw e;
        } finally {
            int status = httpResponse.getStatus();
            boolean isFailure = thrown != null || status >= FAILURE_THRESHOLD;

            if (isFailure) {
                log.warn("REQUEST  [{}] [{} {}]", uuid, method, requestURI);
                log.warn("RESPONSE [{}] [{} {}] [{}]{}",
                        uuid, method, requestURI, status,
                        thrown != null ? " [EXCEPTION] " + thrown.getMessage() : "");
            }
        }
    }
}