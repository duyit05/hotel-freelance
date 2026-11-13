package com.example.back_end.user;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users") // "user" là từ khóa trong PostgreSQL
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Họ và tên hiển thị.
     * Để nullable ở mức CSDL vì bảng cũ đã có dữ liệu (ddl-auto=update không thêm được cột NOT NULL);
     * bắt buộc nhập được kiểm tra ở UserRequest, tài khoản cũ được điền ở DataInitializer.
     */
    @Column(name = "full_name", length = 100)
    private String fullName;

    /** Mật khẩu đã mã hóa BCrypt */
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public User(String username, String fullName, String password, Role role) {
        this.username = username;
        this.fullName = fullName;
        this.password = password;
        this.role = role;
    }
}
