package com.rose.back.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.rose.back.user.dto.UserDTO;
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
}
