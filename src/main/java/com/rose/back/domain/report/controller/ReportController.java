package com.rose.back.domain.report.controller;

import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.report.controller.docs.ReportControllerDocs;
import com.rose.back.domain.report.dto.ReportRequestDto;
import com.rose.back.domain.report.service.ReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController implements ReportControllerDocs {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<Map<String, String>> report(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ReportRequestDto dto) {

        log.info("[POST][/api/reports] - 게시글 신고 요청: {}", dto);
        reportService.reportPost(userDetails.getUserNo(), dto.postId(), dto.reason());
        return ResponseEntity.status(201).body(Map.of("message", "신고가 접수되었습니다."));
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkReport(@RequestParam Long postId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[GET][/api/reports/check] - 신고 여부 확인 요청: postId={}, userId={}", postId, userDetails.getUserNo());
        boolean alreadyReported = reportService.isAlreadyReported(userDetails.getUserNo(), postId);
        return ResponseEntity.ok(Map.of("alreadyReported", alreadyReported));
    }
}