package com.rose.back.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.rose.back.user.dto.UserDTO;
import com.rose.back.user.dto.request.PasswordValidationRequest;
import com.rose.back.user.service.UserService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController implements UserControllerDocs{
    
    private final UserService userService;

    @GetMapping("/data") // 로그인한 사용자 정보 가져오기
    public ResponseEntity<?> get(){
        log.info("사용자 정보 컨트롤러 실행");

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDTO userDto = userService.get(userId);
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/validatePassword")
    public ResponseEntity<Boolean> validatePassword(@RequestBody PasswordValidationRequest request) {
        log.info("비밀번호 확인 컨트롤러 실행");

        boolean isValid = userService.validatePassword(request.getUserId(), request.getUserPwd());
        return ResponseEntity.ok(isValid);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser( @RequestPart("user") UserDTO user) {
        log.info("사용자 정보 수정 컨트롤러 실행");

        try {
            UserDTO updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("정보 수정 실패");
        }
    }
}
