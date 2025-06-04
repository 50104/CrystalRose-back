package com.rose.back.domain.report.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.report.service.CommentReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/comment-reports")
@RequiredArgsConstructor
public class CommentReportController {

    private final CommentReportService commentReportService;

    @PostMapping
    public ResponseEntity<Void> reportComment(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody CommentReportRequestDto dto) {
      
        log.info("[POST][/api/comment-reports] - 댓글 신고 요청: {}", dto);
        commentReportService.reportComment(userDetails.getUserNo(), dto.commentId(), dto.reason());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public record CommentReportRequestDto(Long commentId, String reason) {}
}
