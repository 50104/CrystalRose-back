package com.rose.back.domain.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenProvider {
    
    private SecretKey secretKey;

    public JwtTokenProvider(@Value("${spring.jwt.secret}") String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        // 키가 32바이트보다 작으면 패딩 추가
        keyBytes = Arrays.copyOf(keyBytes, 32);
        this.secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    public String create(String category, String userId, String userNick, String userRole, Long expiredMs, Long userNo) {
        return Jwts.builder()
                .claim("category",category) // access, refresh
                .claim("userNo", userNo)
                .claim("userId", userId)
                .claim("userRole", userRole)
                .claim("userNick", userNick)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    public String getUserId(String token) {
        return getClaims(token).get("userId", String.class);
    }

    public Long getUserNo(String token) {
        return getClaims(token).get("userNo", Long.class);
    }

    public String getUserRole(String token) {
        return getClaims(token).get("userRole", String.class);
    }

    public String getUserNick(String token) {
        return getClaims(token).get("userNick", String.class);
    }

    public String getCategory(String token) {
        return getClaims(token).get("category", String.class);
    }

    public void validateExpiration(String token) throws ExpiredJwtException {
        Date expiration = getClaims(token).getExpiration();
        if (expiration.before(new Date())) {
            throw new ExpiredJwtException(null, null, "JWT has expired");
        }
    }

    // 토큰의 유효성을 검사
    public boolean validateToken(String token) {
        log.info("JWT Secret during validation: {}", secretKey); 
        try {
            getClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    // 토큰 만료시간 계산
    public long getExpiration(String token) {
        Date expiration = getClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
}
