package com.example.back_end.auth;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.back_end.config.SecurityConfig;
import com.example.back_end.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    /** Sai tài khoản/mật khẩu sẽ ném BadCredentialsException, xử lý ở GlobalExceptionHandler (trả 401) */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        var auth = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(req.username(), req.password()));
        return jwtService.issue(auth, userService.fullNameOf(auth.getName()));
    }

    /** Kiểm tra token còn hợp lệ và xem thông tin người đang đăng nhập */
    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(SecurityConfig.ROLES_CLAIM);
        Map<String, Object> me = new LinkedHashMap<>();
        me.put("username", jwt.getSubject());
        me.put("fullName", userService.fullNameOf(jwt.getSubject()));
        me.put("roles", roles);
        me.put("expiresAt", jwt.getExpiresAt());
        return me;
    }
}
