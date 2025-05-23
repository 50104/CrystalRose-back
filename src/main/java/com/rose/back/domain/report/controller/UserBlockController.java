package com.rose.back.domain.report.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.report.service.UserBlockService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/blocks")
@RequiredArgsConstructor
public class UserBlockController {

    private final UserBlockService userBlockService;

    @PostMapping
    public ResponseEntity<Void> block(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestBody BlockRequestDto dto) {
        log.info("[POST][/api/v1/blocks]- 차단 요청: {}", dto);
        userBlockService.blockUser(userDetails.getUserNo(), dto.blockedUserId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public record BlockRequestDto(Long blockedUserId) {}
}