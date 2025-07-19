package com.rose.back.domain.auth.oauth2;

import java.time.LocalDate;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.rose.back.domain.auth.oauth2.response.GoogleResponse;
import com.rose.back.domain.auth.oauth2.response.KakaoResponse;
import com.rose.back.domain.auth.oauth2.response.NaverResponse;
import com.rose.back.domain.auth.oauth2.response.OAuth2Response;
import com.rose.back.domain.user.dto.UserInfoDto;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response oAuthResponse = switch (registrationId) {
            case "naver" -> new NaverResponse(oAuth2User.getAttributes());
            case "kakao" -> new KakaoResponse(oAuth2User.getAttributes());
            case "google" -> new GoogleResponse(oAuth2User.getAttributes());
            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
        };

        String email = normalizeNaverEmail(oAuthResponse.getUserEmail());
        UserEntity user = userRepository.findByUserEmail(email);

        if (user == null) { // 신규 가입
            String prefix = switch (registrationId) {
                case "naver" -> "n_";
                case "kakao" -> "k_";
                case "google" -> "g_";
                default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
            };
            String userId;
            do {
                userId = prefix + String.format("%05d", new Random().nextInt(100_000));
            } while (userRepository.existsByUserId(userId)); // 중복 방지

            String nickname = oAuthResponse.getUserNick() != null ? oAuthResponse.getUserNick() : RandomStringUtils.random(5, true, false);

            user = UserEntity.builder()
                    .userId(userId)
                    .userEmail(email)
                    .userType(oAuthResponse.getProvider())
                    .userNick(nickname)
                    .userRole("ROLE_USER")
                    .userStatus(UserEntity.UserStatus.ACTIVE)
                    .apDate(LocalDate.now())
                    .build();

            userRepository.save(user);
        }

        UserInfoDto userDto = UserInfoDto.builder()
                .userName(user.getUserId())
                .userNick(user.getUserNick())
                .userRole(user.getUserRole())
                .build();

        return new CustomOAuth2User(userDto);
    }

    private String normalizeNaverEmail(String email) {
        if (email != null && email.endsWith("@jr.naver.com")) {
            return email.replace("@jr.naver.com", "@naver.com");
        }
        return email;
    }
}