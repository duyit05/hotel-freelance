package com.example.back_end.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Admin admin, String uploadDir, List<String> corsOrigins) {

    public record Jwt(String secret, Duration expiration) {
    }

    public record Admin(String username, String password, String fullName) {
    }
}
