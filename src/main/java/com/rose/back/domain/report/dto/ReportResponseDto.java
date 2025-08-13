package com.rose.back.domain.report.dto;

import java.time.LocalDateTime;

import com.rose.back.domain.report.entity.Report;

public record ReportResponseDto(
    Long reportId,
    Long boardNo,
    String postTitle,  
    String postContent,
    String reason,
    String reporterNickname,
    String reportedUserNickname,
    LocalDateTime reportedAt
) {
    public static ReportResponseDto from(Report report) {
        return new ReportResponseDto(
            report.getId(),
            report.getTargetPost().getBoardNo(),
            report.getTargetPost().getBoardTitle(),
            report.getTargetPost().getBoardContent(),
            report.getReason(),
            report.getReporter().getUserNick(),
            report.getTargetUser().getUserNick(),
            report.getCreatedAt()
        );
    }
}
