package com.rose.back.domain.user.controller;

import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.user.controller.docs.UserControllerDocs;
import com.rose.back.domain.user.dto.UserInfoDto;
import com.rose.back.domain.user.dto.request.MemberSearchCondition;
import com.rose.back.domain.user.dto.request.PwdValidationRequest;
import com.rose.back.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController implements UserControllerDocs {

    private final UserService userService;

    @GetMapping("/data")
    public ResponseEntity<UserInfoDto> get() {
        log.info("[GET][/api/user/data] - 사용자 정보 조회 요청");
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userService.get(userId));
    }

    @PostMapping("/validatePassword")
    public ResponseEntity<Boolean> validatePassword(@RequestBody PwdValidationRequest request) {
        log.info("[POST][/api/user/validatePassword] - 비밀번호 확인 요청");
        boolean isValid = userService.validatePassword(request.getUserId(), request.getUserPwd());
        return ResponseEntity.ok(isValid);
    }

    @PutMapping("/update")
    public ResponseEntity<UserInfoDto> updateUser(@RequestPart("user") UserInfoDto user) {
        log.info("[PUT][/api/user/update] - 사용자 정보 수정 요청");
        return ResponseEntity.ok(userService.updateUser(user));
    }

    @PostMapping("/modify")
    public ResponseEntity<MessageResponse> modify(@ModelAttribute UserInfoDto userDTO) {
        log.info("[POST][/api/user/modify] - 프로필 이미지 변경 요청");
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        userDTO.setUserName(userId);
        userService.modify(userDTO);
        return ResponseEntity.ok(new MessageResponse("프로필 이미지 변경 완료"));
    }

    @GetMapping("/list")
    public ResponseEntity<List<MemberSearchCondition>> memberList() {
        log.info("[GET][/api/user/list] - 사용자 목록 요청");
        return ResponseEntity.ok(userService.findAll());
    }
}
