package com.example.back_end.auth;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.back_end.user.User;

import jakarta.persistence.LockModeType;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /** Khóa dòng để hai request làm mới cùng lúc không cùng xoay một token */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RefreshToken t join fetch t.user where t.tokenHash = :hash")
    Optional<RefreshToken> findForUpdate(String hash);

    Optional<RefreshToken> findByTokenHash(String hash);

    boolean existsByFamilyIdAndRevokedAtIsNull(String familyId);

    @Modifying
    @Query("update RefreshToken t set t.revokedAt = :now where t.familyId = :familyId and t.revokedAt is null")
    int revokeFamily(String familyId, Instant now);

    @Modifying
    @Query("update RefreshToken t set t.revokedAt = :now where t.user = :user and t.revokedAt is null")
    int revokeAllOf(User user, Instant now);

    @Modifying
    @Query("delete from RefreshToken t where t.user = :user")
    int deleteAllOf(User user);

    @Modifying
    @Query("delete from RefreshToken t where t.expiresAt < :now")
    int deleteExpired(Instant now);
}
