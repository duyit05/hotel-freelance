package com.example.back_end.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Admin admin, String storage, String uploadDir, Minio minio,
        List<String> corsOrigins) {

    /**
     * @param expiration        hạn access token (JWT)
     * @param refreshExpiration hạn refresh token, được gia hạn mỗi lần làm mới
     * @param sessionMaxAge     hạn tối đa của một lần đăng nhập, quá hạn phải đăng nhập lại
     * @param cookieSecure      cookie refresh chỉ gửi qua HTTPS (trình duyệt vẫn cho phép trên http://localhost)
     */
    public record Jwt(String secret, Duration expiration, Duration refreshExpiration, Duration sessionMaxAge,
            boolean cookieSecure) {
    }

    public record Admin(String username, String password, String fullName) {
    }

    public record Minio(String endpoint, String accessKey, String secretKey, String bucket) {
    }
}
