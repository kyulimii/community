package org.example.community.domain.auth.api.dto.request;

public interface CookieConfigProperties {
    boolean httpOnly();
    boolean secure();
    String path();
    String sameSite();
}