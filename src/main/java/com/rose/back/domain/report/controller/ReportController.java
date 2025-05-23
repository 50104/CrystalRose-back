package com.rose.back.domain.report.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<Void> report(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestBody ReportRequestDto dto) {
        log.info("[POST][/api/reports] - 신고 요청: {}", dto);
        reportService.reportPost(userDetails.getUserNo(), dto.postId(), dto.reason());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public record ReportRequestDto(Long postId, String reason) {}
}
