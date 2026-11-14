package com.example.back_end.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Admin admin, String storage, String uploadDir, Minio minio,
        List<String> corsOrigins) {

    public record Jwt(String secret, Duration expiration) {
    }

    public record Admin(String username, String password, String fullName) {
    }

    public record Minio(String endpoint, String accessKey, String secretKey, String bucket) {
    }
}
