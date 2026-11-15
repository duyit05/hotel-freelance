package com.example.back_end.auth;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.example.back_end.config.AppProperties;
import com.example.back_end.config.SecurityConfig;
import com.example.back_end.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder encoder;
    private final AppProperties props;

    /** Tạo access token ngắn hạn chứa tên đăng nhập (sub), họ tên (name), role (roles) và thời hạn (exp) */
    public LoginResponse issue(User user) {
        String name = user.getFullName() == null || user.getFullName().isBlank() ? user.getUsername() : user.getFullName();
        List<String> roles = List.of(user.getRole().name());
        Instant now = Instant.now();
        Instant expiresAt = now.plus(props.jwt().expiration());

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .id(UUID.randomUUID().toString())
            .issuer(SecurityConfig.JWT_ISSUER)
            .issuedAt(now)
            .expiresAt(expiresAt)
            .subject(user.getUsername())
            .claim(SecurityConfig.ROLES_CLAIM, roles)
            .claim(SecurityConfig.NAME_CLAIM, name)
            .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new LoginResponse(token, user.getUsername(), name, roles, expiresAt);
    }
}
