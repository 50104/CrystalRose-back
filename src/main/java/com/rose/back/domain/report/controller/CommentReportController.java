package com.rose.back.domain.report.controller;

import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.report.service.CommentReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/comment-reports")
@RequiredArgsConstructor
public class CommentReportController {

    private final CommentReportService commentReportService;

    @PostMapping
    public ResponseEntity<MessageResponse> reportComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CommentReportRequestDto dto) {

        log.info("[POST][/api/comment-reports] - 댓글 신고 요청: {}", dto);
        commentReportService.reportComment(userDetails.getUserNo(), dto.commentId(), dto.reason());
        return ResponseEntity.status(201).body(new MessageResponse("댓글이 신고되었습니다."));
    }

    public record CommentReportRequestDto(Long commentId, String reason) {}
}
