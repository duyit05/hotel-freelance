package com.example.back_end.auth;

import java.time.Instant;

import com.example.back_end.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Refresh token đã cấp. Chỉ lưu SHA-256 của token, lộ CSDL cũng không dùng được token.
 * Mỗi lần làm mới, token cũ bị thu hồi và cấp token mới cùng "family" (cùng một lần đăng nhập).
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_tokens_family", columnList = "family_id"),
    @Index(name = "idx_refresh_tokens_expires", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Các token sinh ra từ cùng một lần đăng nhập */
    @Column(name = "family_id", nullable = false, length = 36)
    private String familyId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Hạn tối đa của cả phiên đăng nhập, làm mới bao nhiêu lần cũng không vượt quá */
    @Column(name = "session_expires_at", nullable = false)
    private Instant sessionExpiresAt;

    /** Thời điểm token bị thay bằng token mới (null nếu chưa dùng) */
    @Column(name = "rotated_at")
    private Instant rotatedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;
}
