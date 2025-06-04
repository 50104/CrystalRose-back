package com.rose.back.domain.report.dto;

import java.time.LocalDateTime;

public record CommentReportResponseDto(
    Long reportId,
    String reporterNickname,
    String reportedUserNickname,
    String commentContent,
    String reason,
    LocalDateTime reportedAt
) {}