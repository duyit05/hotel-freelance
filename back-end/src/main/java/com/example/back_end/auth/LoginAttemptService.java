package com.example.back_end.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Chống dò mật khẩu: sai MAX_FAILURES lần trong WINDOW thì khóa đăng nhập LOCK_TIME.
 * Đếm theo cặp (tên đăng nhập, IP) để kẻ xấu ở IP khác không khóa được tài khoản của người dùng thật.
 * Lưu trong bộ nhớ (chỉ chạy một instance back-end), khởi động lại thì xóa.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final Duration LOCK_TIME = Duration.ofMinutes(15);

    private record Attempt(int failures, Instant firstFailure, Instant lockedUntil) {
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    /** Ném 429 nếu đang bị khóa */
    public void checkAllowed(String username, String ip) {
        Attempt a = attempts.get(key(username, ip));
        Instant now = Instant.now();
        if (a != null && a.lockedUntil() != null && a.lockedUntil().isAfter(now)) {
            long minutes = Duration.between(now, a.lockedUntil()).toMinutes() + 1;
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "Đăng nhập sai quá nhiều lần, vui lòng thử lại sau " + minutes + " phút");
        }
    }

    public void onFailure(String username, String ip) {
        Instant now = Instant.now();
        attempts.compute(key(username, ip), (k, a) -> {
            if (a == null || a.firstFailure().plus(WINDOW).isBefore(now)) {
                return new Attempt(1, now, null);
            }
            int failures = a.failures() + 1;
            return new Attempt(failures, a.firstFailure(), failures >= MAX_FAILURES ? now.plus(LOCK_TIME) : null);
        });
    }

    public void onSuccess(String username, String ip) {
        attempts.remove(key(username, ip));
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void cleanup() {
        Instant now = Instant.now();
        attempts.values().removeIf(a -> a.firstFailure().plus(WINDOW).isBefore(now)
            && (a.lockedUntil() == null || a.lockedUntil().isBefore(now)));
    }

    private static String key(String username, String ip) {
        return (username == null ? "" : username.trim().toLowerCase()) + "|" + ip;
    }
}
