package org.example.community.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(

        String secret,
        long accessTokenExpSeconds,
        long refreshTokenExpSeconds
) {
}
