package com.rose.back.domain.user.service;

import java.util.List;
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
import com.rose.back.domain.wiki.entity.WikiModificationRequest;
import com.rose.back.domain.wiki.repository.WikiRepository;
import com.rose.back.domain.wiki.repository.WikiModificationRequestRepository;
import com.rose.back.domain.wiki.dto.WikiModificationRequestDto;

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
    private final WikiModificationRequestRepository wikiModificationRequestRepository;

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

    public List<CommentReportResponseDto> getAllCommentReports() {
        List<CommentReport> reports = commentReportRepository.findAll();
        log.info("조회된 댓글 신고 수: {}", reports.size());

        return reports.stream()
            .map(report -> toResponseSafe(report))
            .flatMap(Optional::stream)
            .toList();
    }

    // 도감 수정 요청 관련 메서드들
    public List<WikiModificationRequestDto> getPendingModificationRequests() {
        return wikiModificationRequestRepository.findAllByStatus(WikiModificationRequest.ModificationStatus.PENDING)
                .stream()
                .map(this::toModificationRequestDto)
                .collect(Collectors.toList());
    }

    public void approveModificationRequest(Long requestId) {
        WikiModificationRequest request = getModificationRequestOrThrow(requestId);
        
        // 원본 도감 업데이트
        WikiEntity originalWiki = request.getOriginalWiki();
        updateWikiFromRequest(originalWiki, request);
        
        // 요청 상태 승인으로 변경
        request.setStatus(WikiModificationRequest.ModificationStatus.APPROVED);
        request.setProcessedDate(java.time.LocalDateTime.now());
        
        log.info("도감 수정 요청 ID {} 승인 완료 - 원본 도감 ID {}", requestId, originalWiki.getId());
    }

    public void rejectModificationRequest(Long requestId) {
        WikiModificationRequest request = getModificationRequestOrThrow(requestId);
        request.setStatus(WikiModificationRequest.ModificationStatus.REJECTED);
        request.setProcessedDate(java.time.LocalDateTime.now());
        
        log.info("도감 수정 요청 ID {} 거부 완료", requestId);
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

    private WikiModificationRequest getModificationRequestOrThrow(Long requestId) {
        return wikiModificationRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("수정 요청을 찾을 수 없습니다. ID = " + requestId));
    }

    private void updateWikiFromRequest(WikiEntity wiki, WikiModificationRequest request) {
        wiki.setName(request.getName());
        wiki.setCategory(request.getCategory());
        wiki.setCultivarCode(request.getCultivarCode());
        wiki.setFlowerSize(request.getFlowerSize());
        wiki.setPetalCount(request.getPetalCount());
        wiki.setFragrance(request.getFragrance());
        wiki.setDiseaseResistance(request.getDiseaseResistance());
        wiki.setGrowthType(request.getGrowthType());
        wiki.setUsageType(request.getUsageType());
        wiki.setRecommendedPosition(request.getRecommendedPosition());
        wiki.setContinuousBlooming(request.getContinuousBlooming());
        wiki.setMultiBlooming(request.getMultiBlooming());
        wiki.setGrowthPower(request.getGrowthPower());
        wiki.setColdResistance(request.getColdResistance());
        
        if (request.getImageUrl() != null) {
            wiki.setImageUrl(request.getImageUrl());
        }
    }

    private WikiModificationRequestDto toModificationRequestDto(WikiModificationRequest request) {
        return WikiModificationRequestDto.builder()
                .id(request.getId())
                .originalWikiId(request.getOriginalWiki().getId())
                .requesterNick(request.getRequester().getUserNick())
                .name(request.getName())
                .category(request.getCategory())
                .cultivarCode(request.getCultivarCode())
                .flowerSize(request.getFlowerSize())
                .petalCount(request.getPetalCount())
                .fragrance(request.getFragrance())
                .diseaseResistance(request.getDiseaseResistance())
                .growthType(request.getGrowthType())
                .usageType(request.getUsageType())
                .recommendedPosition(request.getRecommendedPosition())
                .continuousBlooming(request.getContinuousBlooming())
                .multiBlooming(request.getMultiBlooming())
                .growthPower(request.getGrowthPower())
                .coldResistance(request.getColdResistance())
                .imageUrl(request.getImageUrl())
                .description(request.getDescription())
                .status(request.getStatus().name())
                .createdDate(request.getCreatedDate())
                .processedDate(request.getProcessedDate())
                .build();
    }
}