package com.rose.back.domain.report.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rose.back.domain.board.entity.ContentEntity;
import com.rose.back.domain.board.repository.ContentRepository;
import com.rose.back.domain.report.dto.ReportResponseDto;
import com.rose.back.domain.report.entity.Report;
import com.rose.back.domain.report.repository.ReportRepository;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;

    public void reportPost(Long reporterId, Long postId, String reason) {
        log.info("신고자 ID: {}", reporterId);    
        log.info("게시글 ID: {}", postId);
        
        UserEntity reporter = userRepository.findById(reporterId)
            .orElseThrow(() -> new EntityNotFoundException("신고자 없음"));
        ContentEntity post = contentRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("게시글 없음"));
        boolean alreadyReported = reportRepository.existsByReporterAndTargetPost(reporter, post);
        if (alreadyReported) {
            throw new IllegalStateException("이미 해당 게시글을 신고하였습니다.");
        }
        UserEntity targetUser = post.getWriter();

        Report report = new Report();
        report.setReporter(reporter);
        report.setTargetPost(post);
        report.setTargetUser(targetUser);
        report.setReason(reason);

        reportRepository.save(report);
    }

    public List<ReportResponseDto> getAllReports() { // 관리자 신고 내역 조회
        return reportRepository.findAll().stream()
            .map(ReportResponseDto::from)
            .toList();
    }

    public boolean isAlreadyReported(Long reporterId, Long postId) {
        return reportRepository.existsByReporter_UserNoAndTargetPost_BoardNo(reporterId, postId);
    }
}
