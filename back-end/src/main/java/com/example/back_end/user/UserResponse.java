package com.example.back_end.user;

import java.time.Instant;

/** Thông tin tài khoản trả về cho trang quản trị (không bao giờ trả mật khẩu) */
public record UserResponse(Long id, String username, String fullName, Role role, Instant createdAt) {

    static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getFullName(), u.getRole(), u.getCreatedAt());
    }
}
