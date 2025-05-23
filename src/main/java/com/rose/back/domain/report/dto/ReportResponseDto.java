package com.rose.back.domain.report.dto;

import java.time.LocalDateTime;

import com.rose.back.domain.report.entity.Report;

public record ReportResponseDto(
    Long reportId,
    String postContent,
    String reason,
    String reporterNickname,
    String reportedUserNickname,
    LocalDateTime reportedAt
) {
    public static ReportResponseDto from(Report report) {
        return new ReportResponseDto(
            report.getId(),
            report.getTargetPost().getBoardContent(),
            report.getReason(),
            report.getReporter().getUserNick(),
            report.getTargetUser().getUserNick(),
            report.getCreatedAt()
        );
    }
}
