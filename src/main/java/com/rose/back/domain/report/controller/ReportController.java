package com.rose.back.domain.report.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.report.service.ReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<Void> report(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody ReportRequestDto dto) {
        log.info("[POST][/api/reports] - 신고 요청: {}", dto);
        try {
            reportService.reportPost(userDetails.getUserNo(), dto.postId(), dto.reason());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("신고 요청 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkReport(@RequestParam Long postId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[GET][/api/reports/check] - 신고 여부 확인 요청: postId={}, userId={}", postId, userDetails.getUserNo());
        try {
            Long reporterId = userDetails.getUserNo();
            boolean alreadyReported = reportService.isAlreadyReported(reporterId, postId);
            return ResponseEntity.ok(Map.of("alreadyReported", alreadyReported));
        } catch (Exception e) {
            log.error("신고 여부 확인 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("alreadyReported", false));
        }
    }

    public record ReportRequestDto(Long postId, String reason) {}
}