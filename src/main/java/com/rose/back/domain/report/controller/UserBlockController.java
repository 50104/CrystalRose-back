package com.rose.back.domain.report.controller;

import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.report.controller.docs.UserBlockControllerDocs;
import com.rose.back.domain.report.dto.BlockRequestDto;
import com.rose.back.domain.report.dto.UserSummaryDto;
import com.rose.back.domain.report.service.UserBlockService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/blocks")
@RequiredArgsConstructor
public class UserBlockController implements UserBlockControllerDocs {

    private final UserBlockService userBlockService;

    @PostMapping
    public ResponseEntity<MessageResponse> block(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody BlockRequestDto dto) {
        log.info("[POST][/api/v1/blocks] - 사용자 차단 요청: {}", dto);
        userBlockService.blockUser(userDetails.getUserNo(), dto.blockedUserId());
        return ResponseEntity.status(201).body(new MessageResponse("사용자가 차단되었습니다."));
    }

    @GetMapping("/me")
    public ResponseEntity<List<UserSummaryDto>> getBlockedUsers(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[GET][/api/v1/blocks/me] - 차단된 사용자 목록 요청");
        List<UserSummaryDto> blockedUsers = userBlockService.getBlockedUsers(userDetails.getUserNo());
        return ResponseEntity.ok(blockedUsers);
    }

    @DeleteMapping("/{blockedUserId}")
    public ResponseEntity<MessageResponse> unblock(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long blockedUserId) {

        log.info("[DELETE][/api/v1/blocks/{}] - 사용자 차단 해제 요청", blockedUserId);
        userBlockService.unblockUser(userDetails.getUserNo(), blockedUserId);
        return ResponseEntity.ok(new MessageResponse("차단이 해제되었습니다."));
    }
}