package com.rose.back.user.provider;

import java.util.Date;
import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {
    
    @Value("${secret-key}")
    private String secretKey;

    // JWT 토큰 생성 메서드
    public String create(String userId) {

        // 만료 날짜 설정 (현재 시간으로부터 1시간 후)
        Date expiredDate = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)); // 시크릿키 생성

        // json wab token 생성
        String jwt = Jwts.builder()
            .signWith(key, SignatureAlgorithm.HS256)
            .setSubject(userId).setIssuedAt(new Date()).setExpiration(expiredDate)
            .compact();
        
        return jwt;
    }

    // jwt 검증
    public String validate(String jwt) {

        String subject = null;
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        try {
            
            // JWT 토큰 파싱 및 검증
            subject = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt)
                .getBody()
                .getSubject();

        } catch (Exception e) {
            
            e.printStackTrace();
            return null;
        }

        return subject;
    }
}
