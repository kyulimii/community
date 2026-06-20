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

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uuid = UUID.randomUUID().toString();
        String method = httpRequest.getMethod();
        String requestURI = httpRequest.getRequestURI();

        try {
            log.info("REQUEST  [{}] [{} {}]", uuid, method, requestURI);
            chain.doFilter(request, response);
        } finally {
            log.info("RESPONSE [{}] [{} {}] [{}]", uuid, method, requestURI, httpResponse.getStatus());
        }
    }
}
