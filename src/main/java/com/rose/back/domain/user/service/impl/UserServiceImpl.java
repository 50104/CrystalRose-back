package com.rose.back.domain.user.service.impl;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.common.util.ImageValidator;
import com.rose.back.domain.user.dto.UserInfoDto;
import com.rose.back.domain.user.dto.request.MemberSearchCondition;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.user.repository.UserRepository;
import com.rose.back.domain.user.service.UserService;
import com.rose.back.infra.S3.S3Uploader;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Uploader s3Uploader;

    @Override
    public UserInfoDto get(String userId) { // 로그인한 사용자 정보 가져오기
        log.info("로그인한 사용자 정보 추출 컨트롤러 실행");
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new RuntimeException("존재하지 않는 사용자");
        }
        UserInfoDto userDto = UserInfoDto.builder()
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
    public UserInfoDto updateUser(UserInfoDto request) {
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
        return UserInfoDto.builder()
                .userNick(user.getUserNick())
                .userEmail(user.getUserEmail())
                .userName(user.getUserId())
                .userRole(user.getUserRole())
                .userNo(user.getUserNo())
                .userType(user.getUserType())
                .build();
    }

    @Override
    public void modify(UserInfoDto dto) {
        UserEntity user = userRepository.findByUserId(dto.getUserName());
        String beforeUrl = user.getUserProfileImg();

        try {
            boolean isDelete = Boolean.parseBoolean(dto.getIsDelete());
            // 프로필 이미지 삭제 요청
            if (isDelete) {
                if (beforeUrl != null && !beforeUrl.isEmpty()) {
                    s3Uploader.deleteFile(beforeUrl);
                }
                user.setUserProfileImg(null);
            } else if (dto.getUserProfileFile() != null && !dto.getUserProfileFile().isEmpty()) {
                // 새 이미지 업로드
                ImageValidator.validate(dto.getUserProfileFile());
                String uploadedUrl = s3Uploader.uploadProfile(dto.getUserProfileFile(), dto.getUserName());
                user.setUserProfileImg(uploadedUrl);
                if (beforeUrl != null && !beforeUrl.isEmpty()) {
                    s3Uploader.deleteFile(beforeUrl);
                }
            }
            user.setUserNick(dto.getUserNick());
            userRepository.save(user);
            log.info("프로필 이미지 변경 성공: {}", dto.getUserName());
        } catch (IOException e) {
            log.error("S3 프로필 이미지 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("프로필 이미지 처리 실패", e);
        }
    }

    public List<MemberSearchCondition> findAll() {
        List<UserEntity> members = userRepository.findAll();
        List<MemberSearchCondition> memberListResDtos = new ArrayList<>();
        for (UserEntity m : members) {
          MemberSearchCondition memberListResDto = new MemberSearchCondition();
            memberListResDto.setUserNo(m.getUserNo());
            memberListResDto.setUserId(m.getUserId());
            memberListResDto.setUserEmail(m.getUserEmail());
            memberListResDtos.add(memberListResDto);
        }
        return memberListResDtos;
    }

    public boolean isAdmin(String userId) {
        return userRepository.findOptionalByUserId(userId)
            .map(UserEntity::getUserRole)
            .map(role -> role.equals("ROLE_ADMIN"))
            .orElse(false);
    }
}
