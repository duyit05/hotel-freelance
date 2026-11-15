package com.example.back_end.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Phân quyền:
 * - Ai cũng xem được danh sách/chi tiết phòng và ảnh.
 * - Đăng nhập tại POST /api/auth/login để lấy JWT (access token ngắn hạn) + refresh token trong cookie HttpOnly.
 * - POST /api/auth/refresh lấy access token mới, POST /api/auth/logout thu hồi phiên (xem AuthController).
 * - Mọi API còn lại (thêm/sửa/xóa phòng, upload ảnh) cần header "Authorization: Bearer <token>" với role ADMIN.
 */
@Configuration
public class SecurityConfig {

    public static final String JWT_ISSUER = "duynhat-hotel";
    public static final String ROLES_CLAIM = "roles";
    public static final String NAME_CLAIM = "name";

    private final AppProperties props;

    public SecurityConfig(AppProperties props) {
        this.props = props;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // API xác thực bằng header Authorization nên không cần CSRF token.
            // Riêng cookie refresh: SameSite=Strict + bắt buộc header X-Requested-With (xem AuthController)
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/refresh", "/api/auth/logout").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/rooms/**", "/api/room-types/**", "/uploads/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/**").hasRole("ADMIN")
                .anyRequest().denyAll())
            .headers(h -> h
                .frameOptions(f -> f.deny())
                .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
            .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    /* ---------- JWT ký bằng khóa bí mật HS256 ---------- */

    @Bean
    SecretKey jwtSecretKey() {
        byte[] bytes = props.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret phải dài ít nhất 32 ký tự");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey key) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey key) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        // Kiểm tra hạn (exp/nbf) và người phát hành (iss)
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(JWT_ISSUER));
        return decoder;
    }

    /** Đọc claim "roles": ["ADMIN"] trong token thành quyền ROLE_ADMIN */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName(ROLES_CLAIM);
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        return converter;
    }

    /* ---------- Đăng nhập bằng tài khoản lưu trong CSDL ---------- */

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    /* ---------- CORS ---------- */

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(props.corsOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        // Cho phép gửi cookie refresh khi front-end chạy ở origin khác (chỉ các origin trong danh sách)
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
