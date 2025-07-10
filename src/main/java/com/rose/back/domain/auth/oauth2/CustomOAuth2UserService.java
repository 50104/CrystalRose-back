package com.rose.back.domain.auth.oauth2;

import java.time.LocalDate;

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

        String username = oAuthResponse.getProvider() + "" + oAuthResponse.getProviderId();
        if (username.length() > 15) {
            username = username.substring(0, 15);
        }
        UserEntity existData = userRepository.findByUserId(username);
        if (existData == null) {
            //닉네임을 동의하지 않을 경우 랜덤으로 닉네임 등록
            String nickname = oAuthResponse.getUserNick() != null ? oAuthResponse.getUserNick() : RandomStringUtils.random(5, true, false);

            UserEntity user = UserEntity.builder()  
                    .userId(username)
                    .userEmail(oAuthResponse.getUserEmail())
                    .userType(oAuthResponse.getProvider())
                    .userNick(nickname)
                    .userRole("ROLE_USER")
                    .userStatus(UserEntity.UserStatus.ACTIVE) 
                    .apDate(LocalDate.now())
                    .build();
            userRepository.save(user); 

            UserInfoDto userDto = UserInfoDto.builder()
                    .userName(username)
                    .userNick(user.getUserNick())
                    .userRole("ROLE_USER")
                    .build();
            return new CustomOAuth2User(userDto);
        } else {
            existData.setUserEmail(oAuthResponse.getUserEmail());

            UserInfoDto userDto = UserInfoDto.builder()
                    .userName(existData.getUserId())
                    .userNick(existData.getUserNick())
                    .userRole(existData.getUserRole())
                    .build();
            return new CustomOAuth2User(userDto);
        }
    }
}