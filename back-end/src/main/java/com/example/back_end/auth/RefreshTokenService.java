package com.example.back_end.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.back_end.config.AppProperties;
import com.example.back_end.user.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Cấp, xoay vòng và thu hồi refresh token.
 * - Mỗi lần làm mới: token cũ bị thu hồi, cấp token mới (rotation).
 * - Token đã bị thay mà vẫn được gửi lên (sau thời gian ân hạn) => có thể đã bị đánh cắp,
 *   thu hồi toàn bộ phiên đăng nhập đó.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    /** Nhiều tab cùng làm mới gần như đồng thời: tab đến sau vẫn được cấp access token */
    private static final Duration REUSE_GRACE = Duration.ofSeconds(30);

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository tokens;
    private final AppProperties props;

    /** Kết quả cấp token. rawToken = null nghĩa là giữ nguyên cookie (đang trong thời gian ân hạn) */
    public record Issued(User user, String rawToken, Instant expiresAt) {
    }

    /** Tạo phiên mới khi đăng nhập */
    public Issued create(User user) {
        Instant now = Instant.now();
        return save(user, UUID.randomUUID().toString(), now, now.plus(props.jwt().sessionMaxAge()));
    }

    /** Đổi refresh token lấy token mới. Rỗng nếu token không hợp lệ/hết hạn/đã thu hồi */
    public Optional<Issued> rotate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        Optional<RefreshToken> found = tokens.findForUpdate(hash(rawToken));
        if (found.isEmpty()) {
            return Optional.empty();
        }
        RefreshToken token = found.get();
        Instant now = Instant.now();

        if (token.getRevokedAt() != null) {
            boolean justRotated = token.getRotatedAt() != null
                && token.getRotatedAt().plus(REUSE_GRACE).isAfter(now)
                && tokens.existsByFamilyIdAndRevokedAtIsNull(token.getFamilyId());
            if (justRotated) {
                return Optional.of(new Issued(token.getUser(), null, null));
            }
            log.warn("Refresh token đã thu hồi bị dùng lại (user={}), thu hồi cả phiên", token.getUser().getUsername());
            tokens.revokeFamily(token.getFamilyId(), now);
            return Optional.empty();
        }
        if (!token.getExpiresAt().isAfter(now)) {
            return Optional.empty();
        }

        token.setRotatedAt(now);
        token.setRevokedAt(now);
        return Optional.of(save(token.getUser(), token.getFamilyId(), now, token.getSessionExpiresAt()));
    }

    /** Đăng xuất: thu hồi cả phiên chứa token này */
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        tokens.findByTokenHash(hash(rawToken))
            .ifPresent(t -> tokens.revokeFamily(t.getFamilyId(), Instant.now()));
    }

    /** Đăng xuất khỏi mọi thiết bị (vd. khi đổi mật khẩu) */
    public void revokeAll(User user) {
        tokens.revokeAllOf(user, Instant.now());
    }

    public void deleteAll(User user) {
        tokens.deleteAllOf(user);
    }

    /** Dọn token hết hạn mỗi ngày lúc 3h sáng */
    @Scheduled(cron = "0 0 3 * * *")
    public void purgeExpired() {
        int n = tokens.deleteExpired(Instant.now());
        if (n > 0) {
            log.info("Đã xóa {} refresh token hết hạn", n);
        }
    }

    private Issued save(User user, String familyId, Instant now, Instant sessionExpiresAt) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Instant expiresAt = now.plus(props.jwt().refreshExpiration());
        if (expiresAt.isAfter(sessionExpiresAt)) {
            expiresAt = sessionExpiresAt;
        }

        RefreshToken token = new RefreshToken();
        token.setTokenHash(hash(raw));
        token.setUser(user);
        token.setFamilyId(familyId);
        token.setCreatedAt(now);
        token.setExpiresAt(expiresAt);
        token.setSessionExpiresAt(sessionExpiresAt);
        tokens.save(token);
        return new Issued(user, raw, expiresAt);
    }

    private static String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
