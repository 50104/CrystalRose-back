package com.rose.back.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.rose.back.domain.user.controller.docs.UserControllerDocs;
import com.rose.back.domain.user.dto.UserInfoDto;
import com.rose.back.domain.user.dto.request.MemberSearchCondition;
import com.rose.back.domain.user.dto.request.PwdValidationRequest;
import com.rose.back.domain.user.service.UserService;
import com.rose.back.infra.file.FileUtil;

import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController implements UserControllerDocs {

    private final UserService userService;
    private final FileUtil fileUtil;

    @GetMapping("/data")
    public ResponseEntity<?> get() {
        log.info("[GET][/api/user/data] - 사용자 정보 조회 요청");
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            UserInfoDto userDto = userService.get(userId);
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            log.error("사용자 정보 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("조회 실패");
        }
    }

    @PostMapping("/validatePassword")
    public ResponseEntity<?> validatePassword(@RequestBody PwdValidationRequest request) {
        log.info("[POST][/api/user/validatePassword] - 비밀번호 확인 요청");
        try {
            boolean isValid = userService.validatePassword(request.getUserId(), request.getUserPwd());
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            log.error("비밀번호 확인 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("확인 실패");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestPart("user") UserInfoDto user) {
        log.info("[PUT][/api/user/update] - 사용자 정보 수정 요청");
        try {
            UserInfoDto updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("사용자 정보 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("정보 수정 실패");
        }
    }

    @PostMapping("/modify")
    public ResponseEntity<String> modify(@ModelAttribute UserInfoDto userDTO) {
        log.info("[POST][/api/user/modify] - 프로필 이미지 변경 요청");
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            userDTO.setUserName(userId);

            if (userDTO.getUserProfileFile() != null && !userDTO.getUserProfileFile().isEmpty() && !Boolean.parseBoolean(userDTO.getIsDelete())) {
                String userProfileImg = fileUtil.saveFile(userDTO.getUserProfileFile(), userId);
                userDTO.setUserProfileImg(userProfileImg);
            }

            userService.modify(userDTO);
            return ResponseEntity.ok("사진 변경 성공");
        } catch (Exception e) {
            log.error("프로필 이미지 변경 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed.");
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> memberList() {
        log.info("[GET][/api/user/list] - 사용자 목록 조회 요청");
        try {
            List<MemberSearchCondition> dtos = userService.findAll();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("사용자 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("목록 조회 실패");
        }
    }
}
