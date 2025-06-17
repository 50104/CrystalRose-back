package com.rose.back.domain.user.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.rose.back.domain.report.dto.CommentReportResponseDto;
import com.rose.back.domain.report.entity.CommentReport;
import com.rose.back.domain.report.repository.CommentReportRepository;
import com.rose.back.domain.user.dto.AdminResponse;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.comment.entity.CommentEntity;
import com.rose.back.domain.wiki.entity.WikiEntity;
import com.rose.back.domain.wiki.repository.WikiRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final WikiRepository wikiRepository;
    private final CommentReportRepository commentReportRepository;

    public List<AdminResponse> getPendingList() {
        return wikiRepository.findAllByStatus(WikiEntity.Status.PENDING)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void approve(Long id) {
        WikiEntity wiki = getWikiOrThrow(id);
        wiki.setStatus(WikiEntity.Status.APPROVED);
    }

    public void reject(Long id) {
        WikiEntity wiki = getWikiOrThrow(id);
        wiki.setStatus(WikiEntity.Status.REJECTED);
    }

    private WikiEntity getWikiOrThrow(Long id) {
        return wikiRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("도감을 찾을 수 없습니다. ID = " + id));
    }

    private AdminResponse toDto(WikiEntity wiki) {
        return AdminResponse.builder()
                .id(wiki.getId())
                .name(wiki.getName())
                .category(wiki.getCategory())
                .status(wiki.getStatus().name())
                .createdDate(wiki.getCreatedDate())
                .build();
    }

    public List<CommentReportResponseDto> getAllCommentReports() {
        List<CommentReport> reports = commentReportRepository.findAll();
        log.info("조회된 댓글 신고 수: {}", reports.size());

        return reports.stream()
            .map(report -> toResponseSafe(report))
            .flatMap(Optional::stream)
            .toList();
    }

    private Optional<CommentReportResponseDto> toResponseSafe(CommentReport report) {
        try {
            return Optional.of(new CommentReportResponseDto(
                report.getId(),
                Optional.ofNullable(report.getReporter())
                    .map(UserEntity::getUserNick)
                    .orElse("탈퇴한 유저"),
                Optional.ofNullable(report.getTargetComment())
                    .flatMap(comment -> Optional.ofNullable(comment.getWriter())
                        .map(UserEntity::getUserNick))
                    .orElse("알 수 없음"),
                Optional.ofNullable(report.getTargetComment())
                    .map(CommentEntity::getContent)
                    .orElse("댓글이 삭제됨"),
                report.getReason(),
                report.getReportedAt()
            ));
        } catch (Exception e) {
            log.error("Report 변환 중 예외 발생: reportId={}, message={}", report.getId(), e.getMessage(), e);
            return Optional.empty();
        }
    }
}