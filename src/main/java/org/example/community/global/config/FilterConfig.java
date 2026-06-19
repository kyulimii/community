package org.example.community.global.config;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.example.community.global.filter.LogFilter;
import org.example.community.global.filter.LoginCheckFilter;
import org.example.community.global.jwt.JwtProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final JwtProvider jwtProvider;

    @Bean
    public FilterRegistrationBean<LogFilter> logFilter() {
        FilterRegistrationBean<LogFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new LogFilter());
        registrationBean.setOrder(1);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setDispatcherTypes(DispatcherType.REQUEST);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<LoginCheckFilter> loginCheckFilter() {
        FilterRegistrationBean<LoginCheckFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new LoginCheckFilter(jwtProvider));
        registrationBean.setOrder(2);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setDispatcherTypes(DispatcherType.REQUEST);
        return registrationBean;
    }
}
