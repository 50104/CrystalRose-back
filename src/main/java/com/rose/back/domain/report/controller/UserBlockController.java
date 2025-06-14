package com.rose.back.domain.report.controller;


import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.report.dto.UserSummaryDto;
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
    public ResponseEntity<Void> block(@AuthenticationPrincipal CustomUserDetails userDetails,@RequestBody BlockRequestDto dto) {
        log.info("[POST][/api/v1/blocks] - 차단 요청: {}", dto);
        try {
            userBlockService.blockUser(userDetails.getUserNo(), dto.blockedUserId());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("사용자 차단 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<List<UserSummaryDto>> getBlockedUsers(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[GET][/api/v1/blocks/me] - 차단된 사용자 목록 요청");
        try {
            List<UserSummaryDto> blockedUsers = userBlockService.getBlockedUsers(userDetails.getUserNo());
            return ResponseEntity.ok(blockedUsers);
        } catch (Exception e) {
            log.error("차단된 사용자 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(List.of()); // 빈 리스트 반환
        }
    }

    @DeleteMapping("/{blockedUserId}")
    public ResponseEntity<Void> unblock(@AuthenticationPrincipal CustomUserDetails userDetails,@PathVariable Long blockedUserId) {
        log.info("[DELETE][/api/v1/blocks/{}] - 차단 해제 요청", blockedUserId);
        try {
            userBlockService.unblockUser(userDetails.getUserNo(), blockedUserId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("사용자 차단 해제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public record BlockRequestDto(Long blockedUserId) {}
}