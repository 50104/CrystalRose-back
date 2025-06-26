package com.rose.back.domain.report.service;

import org.springframework.stereotype.Service;

import com.rose.back.domain.comment.entity.CommentEntity;
import com.rose.back.domain.comment.repository.CommentRepository;
import com.rose.back.domain.report.entity.CommentReport;
import com.rose.back.domain.report.repository.CommentReportRepository;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentReportService {

    private final CommentReportRepository commentReportRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public void reportComment(Long reporterId, Long commentId, String reason) {
        UserEntity reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new EntityNotFoundException("신고자 없음"));
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글 없음"));

        if (commentReportRepository.existsByReporterAndTargetComment(reporter, comment)) {
            throw new IllegalStateException("이미 신고한 댓글입니다.");
        }

        CommentReport report = new CommentReport();
        report.setReporter(reporter);
        report.setTargetComment(comment);
        report.setReason(reason);
        commentReportRepository.save(report);
    }

    public boolean isAlreadyReported(Long userId, Long commentId) {
        UserEntity reporter = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("사용자 없음"));
        CommentEntity comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new EntityNotFoundException("댓글 없음"));
        return commentReportRepository.existsByReporterAndTargetComment(reporter, comment);
    }
}
