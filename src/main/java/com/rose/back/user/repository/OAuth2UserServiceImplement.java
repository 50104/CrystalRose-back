package com.rose.back.user.repository;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rose.back.user.repository.UserRepository;
import com.rose.back.user.entity.CustomOAuth2User;
import com.rose.back.user.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuth2UserServiceImplement extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
  
    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
    
        OAuth2User oAuth2User = super.loadUser(request);
        String oauthClientName = request.getClientRegistration().getClientName();

        try{
          System.out.println(new ObjectMapper().writeValueAsString(oAuth2User.getAttributes()));
        } catch(Exception exception) {
          exception.printStackTrace();
        }

        UserEntity userEntity = null;
        String userId = null;
        String userEmail = null;

        // 카카오 로그인시 id 값 가져오기
        if(oauthClientName.equals("kakao")) {
          userId = "kakao_" + oAuth2User.getAttributes().get("id");
          userEntity = new UserEntity(userId, userEmail, "kakao");
        }

        // 네이버 로그인시 id 값 가져올때 response로 넘어오면 그안에서 id랑 userEmail 값 가져오기
        if(oauthClientName.equals("naver")) {
          Map<String, String> responseMap = (Map<String, String>) oAuth2User.getAttributes().get("response");
          userId = "naver" + responseMap.get("id").substring(0, 14);
          userEmail = responseMap.get("email");
          userEntity = new UserEntity(userId, userEmail, "naver");
        }
        // 넘어온 데이터 전부 저장
        userRepository.save(userEntity);

        // CustomOAuth2User Entity에서 getName으로 userId 꺼내오기
        return new CustomOAuth2User(userId);
      
    }

}