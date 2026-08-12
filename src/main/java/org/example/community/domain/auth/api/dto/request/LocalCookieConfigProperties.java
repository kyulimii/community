package org.example.community.domain.auth.api.dto.request;

import java.time.Duration;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

@Profile("local")
@ConfigurationProperties(prefix = "server.servlet")
public record LocalCookieConfigProperties(
//        String accessName,

//        String name,

        boolean httpOnly,

        boolean secure,

        @Nullable String path,

        Duration maxAge,

        @Nullable String sameSite
) implements CookieConfigProperties {
}
