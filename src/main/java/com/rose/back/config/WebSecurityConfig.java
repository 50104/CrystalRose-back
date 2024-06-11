package com.rose.back.config;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

import com.rose.back.user.filter.JwtAuthenticationFilter;
import com.rose.back.user.handler.OAuth2SuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configurable
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final DefaultOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    protected SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {
        
        httpSecurity
            .cors(cors -> cors
                .configurationSource(corsConfigurationSource())
            )
            .csrf(CsrfConfigurer::disable) // CSRF(Cross-Site Request Forgery) 비활성화
            .httpBasic(HttpBasicConfigurer::disable) // HTTP 기본 인증 비활성화
            .sessionManagement(sessionManagement -> sessionManagement
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 생성 정책 설정 (STATELESS: 세션을 사용하지 않음)
            )
            .authorizeHttpRequests(request -> request
                .requestMatchers("/","/api/v1/auth/**","/oauth2/**").permitAll() // 특정 URL 패턴에 대한 접근 권한 설정
                .requestMatchers("/api/v1/user/**").hasRole("USER") // 특정 URL 패턴에 대한 접근 권한 설정 (USER 역할 필요)
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN") // 특정 URL 패턴에 대한 접근 권한 설정 (ADMIN 역할 필요)
                .anyRequest().authenticated() // 모든 요청에 대해 인증이 필요함
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint -> endpoint.baseUri("/api/v1/auth/oauth2")) // OAuth2 인증 엔드포인트 설정
                .redirectionEndpoint(endpoint -> endpoint.baseUri("/oauth2/callback/*")) // OAuth2 리다이렉션 엔드포인트 설정
                .userInfoEndpoint(endpoint -> endpoint.userService(oAuth2UserService)) // OAuth2 사용자 정보 엔드포인트 설정
                .successHandler(oAuth2SuccessHandler) // OAuth2 인증 성공 핸들러 설정
            )
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(new FailedAuthenticationEntryPoint()) // 인증 실패 핸들러 설정
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // JWT 인증 필터 추가

        return httpSecurity.build();
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("http://localhost:3000"); // 허용된 오리진 설정 (리액트 서버)
        corsConfiguration.addAllowedMethod("*"); // 허용된 HTTP 메서드 설정
        corsConfiguration.addAllowedHeader("*"); // 허용된 헤더 설정

        corsConfiguration.setAllowCredentials(true); // 자격 증명 허용 설정
        corsConfiguration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Headers",
                "Authorization, x-xsrf-token, Access-Control-Allow-Headers, Origin, Accept, X-Requested-With, " +
                        "Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers")); // 노출된 헤더 설정

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }
    // xx cors 에러2 같이 확인 "/api/v1/**" 변경
}

// 인증 실패 핸들러 클래스
class FailedAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        response.setContentType("application/json"); // 응답의 Content-Type을 JSON으로 설정
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 응답의 상태 코드를 403 Forbidden으로 설정
        response.getWriter().write("{\"code\": \"NP\", \"message\": \"No Permission.\"}"); // 응답의 내용을 JSON 형식으로 작성하여 출력
    }
}

///////////////////////////////////////////////

//4차 부트3
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
// import org.springframework.security.web.SecurityFilterChain;

// @Configuration
// public class WebSecurityConfig {

//     @Bean
//     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http.csrf(AbstractHttpConfigurer::disable);
//         return http.build();
//     }

// }

///////////////////////////////////////////////

//3차 부트2
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.ecurity.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// import org.springframework.web.filter.CorsFilter;

// @Configuration
// @EnableWebSecurity
// public class SecurityConfig extends WebSecurityConfigurerAdapter {

//     @Override
//     protected void configure(HttpSecurity http) throws Exception {
//         http.cors().and().csrf().disable();
//     }

//     @Bean
//     public CorsFilter corsFilter() {
//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         CorsConfiguration config = new CorsConfiguration();
//         config.setAllowCredentials(true);
//         config.addAllowedOrigin("http://localhost:3000");
//         config.addAllowedHeader("*");
//         config.addAllowedMethod("*");
//         source.registerCorsConfiguration("/**", config);
//         return new CorsFilter(source);
//     }
// }