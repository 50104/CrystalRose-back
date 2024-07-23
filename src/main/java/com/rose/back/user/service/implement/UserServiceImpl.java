package com.rose.back.user.service.implement;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.user.dto.UserDTO;
import com.rose.back.user.entity.UserEntity;
import com.rose.back.user.repository.UserRepository;
import com.rose.back.user.service.UserService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO get(String userId) { // 로그인한 사용자 정보 가져오기
        log.info("로그인한 사용자 정보 추출 컨트롤러 실행");
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new RuntimeException("존재하지 않는 사용자");
        }
        UserDTO userDto = UserDTO.builder()
                .userNick(user.getUserNick())
                .userEmail(user.getUserEmail())
                .userProfileImg(user.getUserProfileImg())
                .userName(user.getUserId())
                .userRole(user.getUserRole())
                .userNo(user.getUserNo())
                .userType(user.getUserType())
                .userPwd(user.getUserPwd())
                .build();
        return userDto;
    }

    @Override
    public boolean validatePassword(String userId, String userPwd) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) {
            return false;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(userPwd, user.getUserPwd());
    }

    @Override
    public UserDTO updateUser(UserDTO request) {
        UserEntity user = userRepository.findByUserId(request.getUserName());
        if (user == null) {
            throw new RuntimeException("존재하지 않는 사용자");
        }
        user.setUserNick(request.getUserNick());
        user.setUserEmail(request.getUserEmail());

        if (request.getUserRole() != null) {
            user.setUserRole(request.getUserRole());
        }
        if (request.getUserType() != null) {
            user.setUserType(request.getUserType());
        }
        if (request.getUserPwd() != null && !request.getUserPwd().isEmpty()) {
            user.setUserPwd(passwordEncoder.encode(request.getUserPwd()));
        }
        userRepository.save(user);
        return UserDTO.builder()
                .userNick(user.getUserNick())
                .userEmail(user.getUserEmail())
                .userName(user.getUserId())
                .userRole(user.getUserRole())
                .userNo(user.getUserNo())
                .userType(user.getUserType())
                .build();
    }
}
