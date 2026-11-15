package com.example.back_end.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.back_end.config.AppProperties;
import com.example.back_end.config.SecurityConfig;
import com.example.back_end.user.UserRepository;
import com.example.back_end.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Đăng nhập dùng 2 loại token:
 * - Access token (JWT, ~15 phút): trả trong body, front-end giữ trong bộ nhớ và gửi qua header Authorization.
 * - Refresh token (chuỗi ngẫu nhiên, vài ngày): nằm trong cookie HttpOnly chỉ gửi tới /api/auth,
 *   JavaScript không đọc được nên XSS không lấy được. Dùng để xin access token mới qua POST /api/auth/refresh.
 *
 * refresh/logout dùng cookie nên bắt buộc header X-Requested-With (trang web lạ không gửi được header này
 * nếu không qua CORS), cộng với cookie SameSite=Strict để chống CSRF.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    public static final String REFRESH_COOKIE = "hotel_refresh";
    private static final String COOKIE_PATH = "/api/auth";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokens;
    private final LoginAttemptService loginAttempts;
    private final UserRepository users;
    private final UserService userService;
    private final AppProperties props;

    /** Sai tài khoản/mật khẩu sẽ ném BadCredentialsException, xử lý ở GlobalExceptionHandler (trả 401) */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        String ip = http.getRemoteAddr();
        loginAttempts.checkAllowed(req.username(), ip);
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(req.username(), req.password()));
        } catch (AuthenticationException e) {
            loginAttempts.onFailure(req.username(), ip);
            throw e;
        }
        loginAttempts.onSuccess(req.username(), ip);

        var user = users.findByUsername(req.username()).orElseThrow();
        var issued = refreshTokens.create(user);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, refreshCookie(issued.rawToken(), issued.expiresAt()).toString())
            .body(jwtService.issue(user));
    }

    /** Đổi refresh token (cookie) lấy access token mới, đồng thời xoay vòng refresh token */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String rawToken,
            @RequestHeader(name = "X-Requested-With", required = false) String requestedWith) {
        requireAjax(requestedWith);
        var issued = refreshTokens.rotate(rawToken).orElse(null);
        if (issued == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.SET_COOKIE, clearCookie().toString())
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                    "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại"));
        }
        var res = ResponseEntity.ok();
        if (issued.rawToken() != null) {
            res.header(HttpHeaders.SET_COOKIE, refreshCookie(issued.rawToken(), issued.expiresAt()).toString());
        }
        return res.body(jwtService.issue(issued.user()));
    }

    /** Thu hồi phiên hiện tại và xóa cookie */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE, required = false) String rawToken,
            @RequestHeader(name = "X-Requested-With", required = false) String requestedWith) {
        requireAjax(requestedWith);
        refreshTokens.revoke(rawToken);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, clearCookie().toString()).build();
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

    private static void requireAjax(String requestedWith) {
        if (!"XMLHttpRequest".equals(requestedWith)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Yêu cầu không hợp lệ");
        }
    }

    private ResponseCookie refreshCookie(String value, Instant expiresAt) {
        return baseCookie(value).maxAge(Duration.between(Instant.now(), expiresAt)).build();
    }

    private ResponseCookie clearCookie() {
        return baseCookie("").maxAge(0).build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(REFRESH_COOKIE, value)
            .httpOnly(true)
            .secure(props.jwt().cookieSecure())
            .sameSite("Strict")
            .path(COOKIE_PATH);
    }
}
