package com.rose.back.domain.auth.oauth2;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.rose.back.domain.user.dto.UserInfoDto;

import java.util.*;

public class CustomOAuth2User implements OAuth2User {

    private final UserInfoDto userDTO;

    public CustomOAuth2User(UserInfoDto userDTO) {
        this.userDTO = userDTO;
    }   

    @Override
    public Map<String, Object> getAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", userDTO.getUserName());
        attributes.put("nickname", userDTO.getUserNick());
        attributes.put("email", userDTO.getUserEmail());
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {// 권한 부여
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userDTO.getUserRole();
            }
        });
        return collection;
    }

    @Override
    public String getName() {
        return userDTO.getUserNick();
    }

    public String getUsername() {
        return userDTO.getUserName();
    }
}