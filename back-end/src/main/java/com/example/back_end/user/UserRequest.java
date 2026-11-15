package com.example.back_end.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dữ liệu thêm/sửa tài khoản.
 * - username: chỉ dùng khi thêm mới (không đổi được sau khi tạo, vì JWT đang lưu username).
 * - password: bắt buộc khi thêm mới; khi sửa để trống nghĩa là giữ mật khẩu cũ.
 */
public record UserRequest(
    @Pattern(regexp = "^[a-zA-Z0-9._-]{3,50}$",
        message = "Tên đăng nhập 3–50 ký tự, chỉ gồm chữ không dấu, số, dấu chấm, gạch dưới, gạch ngang")
    String username,

    @NotBlank(message = "Vui lòng nhập họ và tên")
    @Size(max = 100, message = "Họ và tên tối đa 100 ký tự")
    String fullName,

    @Size(min = 8, max = 100, message = "Mật khẩu từ 8 đến 100 ký tự")
    String password) {

    /** Chuỗi rỗng coi như không nhập */
    public UserRequest {
        username = blankToNull(username);
        fullName = fullName == null ? null : fullName.trim();
        password = password == null || password.isEmpty() ? null : password;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
