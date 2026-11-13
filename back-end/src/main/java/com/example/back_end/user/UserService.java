package com.example.back_end.user;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.back_end.common.FieldValidationException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return users.findAll(Sort.by("id")).stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(get(id));
    }

    @Transactional(readOnly = true)
    public String fullNameOf(String username) {
        return users.findByUsername(username).map(User::getFullName).orElse(null);
    }

    public UserResponse create(UserRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (req.username() == null) {
            errors.put("username", "Vui lòng nhập tên đăng nhập");
        } else if (users.existsByUsername(req.username())) {
            errors.put("username", "Tên đăng nhập \"" + req.username() + "\" đã tồn tại");
        }
        if (req.password() == null) {
            errors.put("password", "Vui lòng nhập mật khẩu");
        }
        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
        User user = new User(req.username(), req.fullName(), passwordEncoder.encode(req.password()), Role.ADMIN);
        return UserResponse.from(users.save(user));
    }

    /** Không đổi username; chỉ đổi mật khẩu khi có nhập */
    public UserResponse update(Long id, UserRequest req) {
        User user = get(id);
        user.setFullName(req.fullName());
        if (req.password() != null) {
            user.setPassword(passwordEncoder.encode(req.password()));
        }
        return UserResponse.from(user);
    }

    public void delete(Long id, String currentUsername) {
        User user = get(id);
        if (user.getUsername().equals(currentUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Không thể tự xóa tài khoản đang đăng nhập");
        }
        if (user.getRole() == Role.ADMIN && users.countByRole(Role.ADMIN) <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phải còn ít nhất một tài khoản quản trị");
        }
        users.delete(user);
    }

    private User get(Long id) {
        return users.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
    }
}
