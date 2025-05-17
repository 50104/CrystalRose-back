package com.rose.back.domain.auth.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.user.dto.UserInfoDto;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    // 사용자 이름을 받아 DB에서 사용자 정보를 조회
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUserId(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        
        UserInfoDto userDto = UserInfoDto.builder()
                .userName(user.getUserId())
                .userPwd(user.getUserPwd())
                .userRole(user.getUserRole())
                .userNick(user.getUserNick())
                .build();
        return new CustomUserDetails(userDto); // UserDetails에 담아서 return하면 AuthenticationManager가 검증
    }
}
