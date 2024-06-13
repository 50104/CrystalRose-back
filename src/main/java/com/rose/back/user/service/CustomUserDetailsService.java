package com.rose.back.user.service;

import com.rose.back.user.dto.dto2.CustomUserDetails;
import com.rose.back.user.entity.UserEntity;
import com.rose.back.user.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    // 사용자 이름을 받아 DB에서 사용자 정보를 조회하는 메소드
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        // DB에서 조회
        UserEntity userData = userRepository.findByUserId(userId);

        if (userData == null) {
            throw new UsernameNotFoundException("User not found with userId: " + userId);
        }

        // UserDetails에 담아서 return하면 AuthenticationManager가 검증 함
        return new CustomUserDetails(userData);
    }
}
