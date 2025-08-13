package com.rose.back.domain.user.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.domain.report.dto.CommentReportResponseDto;
import com.rose.back.domain.report.entity.CommentReport;
import com.rose.back.domain.report.repository.CommentReportRepository;
import com.rose.back.domain.user.dto.AdminResponse;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.board.entity.ContentEntity;
import com.rose.back.domain.comment.entity.CommentEntity;
import com.rose.back.domain.wiki.entity.WikiEntity;
import com.rose.back.domain.wiki.entity.WikiModificationRequest;
import com.rose.back.domain.wiki.repository.WikiRepository;
import com.rose.back.domain.wiki.service.WikiImageService;
import com.rose.back.domain.wiki.repository.WikiModificationRequestRepository;
import com.rose.back.domain.wiki.dto.WikiModificationRequestDto;
import com.rose.back.domain.wiki.dto.WikiDetailResponse;
import com.rose.back.domain.wiki.dto.WikiModificationComparisonDto;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final WikiRepository wikiRepository;
    private final WikiImageService wikiImageService;
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
        return wikiModificationRequestRepository.findAll()
                .stream()
                .map(this::toModificationRequestDto)
                .collect(Collectors.toList());
    }

    public void approveModificationRequest(Long requestId) {
        WikiModificationRequest request = getModificationRequestOrThrow(requestId);
        WikiEntity originalWiki = request.getOriginalWiki();
        updateWikiFromRequest(originalWiki, request);

        wikiImageService.wikiModification(
            request.getImageUrl(),
            originalWiki
        );

        originalWiki.setModificationStatus(WikiEntity.ModificationStatus.NONE);
        wikiModificationRequestRepository.delete(request);

        log.info("도감 수정 요청 ID {} 승인 완료 - 원본 도감 ID {} 업데이트 및 요청 삭제", requestId, originalWiki.getId());
    }

    public void rejectModificationRequest(Long requestId, String reason) {
        WikiModificationRequest request = getModificationRequestOrThrow(requestId);
        request.setStatus(WikiModificationRequest.Status.REJECTED);
        request.setDescription(reason); // 사유 저장

        WikiEntity original = request.getOriginalWiki();
        original.setModificationStatus(WikiEntity.ModificationStatus.NONE);

        log.info("도감 수정 요청 ID {} 거부 완료 (사유: {})", requestId, reason);
    }

    // 도감 수정 요청 비교
    public WikiModificationComparisonDto getModificationComparison(Long requestId) {
        WikiModificationRequest request = getModificationRequestOrThrow(requestId);
        WikiEntity originalWiki = request.getOriginalWiki();
        
        // 원본 데이터
        WikiModificationComparisonDto.WikiDataDto originalData = WikiModificationComparisonDto.WikiDataDto.builder()
                .name(originalWiki.getName())
                .category(originalWiki.getCategory())
                .cultivarCode(originalWiki.getCultivarCode())
                .flowerSize(originalWiki.getFlowerSize())
                .petalCount(originalWiki.getPetalCount())
                .fragrance(originalWiki.getFragrance())
                .diseaseResistance(originalWiki.getDiseaseResistance())
                .growthType(originalWiki.getGrowthType())
                .usageType(originalWiki.getUsageType())
                .recommendedPosition(originalWiki.getRecommendedPosition())
                .continuousBlooming(originalWiki.getContinuousBlooming())
                .multiBlooming(originalWiki.getMultiBlooming())
                .growthPower(originalWiki.getGrowthPower())
                .coldResistance(originalWiki.getColdResistance())
                .imageUrl(originalWiki.getImageUrl())
                .build();
        
        // 수정된 데이터
        WikiModificationComparisonDto.WikiDataDto modifiedData = WikiModificationComparisonDto.WikiDataDto.builder()
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
                .build();
        
        // 변경된 필드 찾기
        List<String> changedFields = findChangedFields(originalData, modifiedData);
        
        return WikiModificationComparisonDto.builder()
                .requestId(request.getId())
                .originalWikiId(originalWiki.getId())
                .requesterNick(request.getRequester().getUserNick())
                .description(request.getDescription())
                .createdDate(request.getCreatedDate())
                .originalData(originalData)
                .modifiedData(modifiedData)
                .changedFields(changedFields)
                .build();
    }
    
    private List<String> findChangedFields(WikiModificationComparisonDto.WikiDataDto original, WikiModificationComparisonDto.WikiDataDto modified) {
        List<String> changedFields = new java.util.ArrayList<>();
        
        if (!java.util.Objects.equals(original.getName(), modified.getName())) {
            changedFields.add("name");
        }
        if (!java.util.Objects.equals(original.getCategory(), modified.getCategory())) {
            changedFields.add("category");
        }
        if (!java.util.Objects.equals(original.getCultivarCode(), modified.getCultivarCode())) {
            changedFields.add("cultivarCode");
        }
        if (!java.util.Objects.equals(original.getFlowerSize(), modified.getFlowerSize())) {
            changedFields.add("flowerSize");
        }
        if (!java.util.Objects.equals(original.getPetalCount(), modified.getPetalCount())) {
            changedFields.add("petalCount");
        }
        if (!java.util.Objects.equals(original.getFragrance(), modified.getFragrance())) {
            changedFields.add("fragrance");
        }
        if (!java.util.Objects.equals(original.getDiseaseResistance(), modified.getDiseaseResistance())) {
            changedFields.add("diseaseResistance");
        }
        if (!java.util.Objects.equals(original.getGrowthType(), modified.getGrowthType())) {
            changedFields.add("growthType");
        }
        if (!java.util.Objects.equals(original.getUsageType(), modified.getUsageType())) {
            changedFields.add("usageType");
        }
        if (!java.util.Objects.equals(original.getRecommendedPosition(), modified.getRecommendedPosition())) {
            changedFields.add("recommendedPosition");
        }
        if (!java.util.Objects.equals(original.getContinuousBlooming(), modified.getContinuousBlooming())) {
            changedFields.add("continuousBlooming");
        }
        if (!java.util.Objects.equals(original.getMultiBlooming(), modified.getMultiBlooming())) {
            changedFields.add("multiBlooming");
        }
        if (!java.util.Objects.equals(original.getGrowthPower(), modified.getGrowthPower())) {
            changedFields.add("growthPower");
        }
        if (!java.util.Objects.equals(original.getColdResistance(), modified.getColdResistance())) {
            changedFields.add("coldResistance");
        }
        if (!java.util.Objects.equals(original.getImageUrl(), modified.getImageUrl())) {
            changedFields.add("imageUrl");
        }
        
        return changedFields;
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
            Long commentId = Optional.ofNullable(report.getTargetComment())
                .map(CommentEntity::getId)
                .orElse(null);

            Long contentId = Optional.ofNullable(report.getTargetComment())
                .map(CommentEntity::getContentEntity)
                .map(ContentEntity::getBoardNo)
                .orElse(null);

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
                report.getReportedAt(),
                commentId,
                contentId
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
                .originalName(request.getOriginalWiki().getName())
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
                .createdDate(request.getCreatedDate())
                .build();
    }

    @Transactional
    public void deleteWikiByAdmin(Long wikiId) {
        WikiEntity wiki = wikiRepository.findById(wikiId)
            .orElseThrow(() -> new EntityNotFoundException("도감을 찾을 수 없습니다."));
        wiki.setStatus(WikiEntity.Status.DELETED);
    }

    public WikiDetailResponse getWikiDetailByAdmin(Long id) {
        WikiEntity wiki = getWikiOrThrow(id);

        return WikiDetailResponse.builder()
                .id(wiki.getId())
                .name(wiki.getName())
                .category(wiki.getCategory())
                .cultivarCode(wiki.getCultivarCode())
                .flowerSize(wiki.getFlowerSize())
                .petalCount(wiki.getPetalCount())
                .fragrance(wiki.getFragrance())
                .diseaseResistance(wiki.getDiseaseResistance())
                .growthType(wiki.getGrowthType())
                .usageType(wiki.getUsageType())
                .recommendedPosition(wiki.getRecommendedPosition())
                .continuousBlooming(wiki.getContinuousBlooming())
                .multiBlooming(wiki.getMultiBlooming())
                .growthPower(wiki.getGrowthPower())
                .coldResistance(wiki.getColdResistance())
                .imageUrl(wiki.getImageUrl())
                .status(wiki.getStatus().name())
                .modificationStatus(wiki.getModificationStatus().name())
                .createdDate(wiki.getCreatedDate())
                .build();
    }
}