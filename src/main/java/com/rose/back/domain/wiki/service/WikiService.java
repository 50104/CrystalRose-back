package com.rose.back.domain.wiki.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.rose.back.domain.wiki.dto.WikiModificationListDto;
import com.rose.back.domain.wiki.dto.WikiModificationResubmitDto;
import com.rose.back.domain.wiki.dto.WikiRequest;
import com.rose.back.domain.wiki.dto.WikiResponse;
import com.rose.back.domain.wiki.entity.WikiEntity;
import com.rose.back.domain.wiki.entity.WikiModificationRequest;
import com.rose.back.domain.wiki.entity.WikiModificationRequest.Status;
import com.rose.back.domain.wiki.repository.WikiRepository;
import com.rose.back.domain.wiki.repository.WikiModificationRequestRepository;
import com.rose.back.domain.user.entity.UserEntity;

import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WikiService {

    private final WikiRepository wikiRepository;
    private final WikiImageService wikiImageService;
    private final WikiModificationRequestRepository wikiModificationRequestRepository;

    public void registerWiki(WikiRequest dto) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            WikiEntity wiki = WikiEntity.builder()
                .name(dto.getName())
                .category(dto.getCategory())
                .cultivarCode(defaultValue(dto.getCultivarCode(), "-"))
                .flowerSize(defaultValue(dto.getFlowerSize(), "-"))
                .petalCount(defaultValue(dto.getPetalCount(), "-"))
                .fragrance(defaultValue(dto.getFragrance(), "-"))
                .diseaseResistance(defaultValue(dto.getDiseaseResistance(), "-"))
                .growthType(defaultValue(dto.getGrowthType(), "-"))
                .usageType(defaultValue(dto.getUsageType(), "-"))
                .recommendedPosition(defaultValue(dto.getRecommendedPosition(), "-"))
                .imageUrl(dto.getImageUrl())
                .continuousBlooming(defaultValue(dto.getContinuousBlooming(), "-"))
                .multiBlooming(defaultValue(dto.getMultiBlooming(), "-"))
                .growthPower(defaultValue(dto.getGrowthPower(), "-"))
                .coldResistance(defaultValue(dto.getColdResistance(), "-"))
                .status(WikiEntity.Status.PENDING)
                .modificationStatus(WikiEntity.ModificationStatus.NONE)
                .createdBy(Long.parseLong(userId))
                .build();

            wikiRepository.save(wiki);
            wikiImageService.saveAndBindImage(dto.getImageUrl(), wiki);

            log.info("도감 등록 완료 - 승인 대기 중: {}", dto.getName());

        } catch (Exception e) {
            log.error("도감 등록 중 예외 발생: {}", e.getMessage(), e);
            throw new RuntimeException("도감 등록 실패", e);
        }
    }

    private String defaultValue(String value, String fallback) {
        return (value == null || value.trim().isEmpty()) ? fallback : value;
    }

    public void submitModificationRequest(Long wikiId, WikiRequest dto, UserEntity requester) {
        log.info("도감 수정 요청 처리 시작 - wikiId: {}, requester: {}", wikiId, requester.getUserId());
        
        WikiEntity originalWiki = wikiRepository.findById(wikiId)
            .orElseThrow(() -> new RuntimeException("수정할 도감 정보를 찾을 수 없습니다. ID: " + wikiId));
        
        // 이미 수정 요청이 진행 중인지 확인
        if (originalWiki.getModificationStatus() == WikiEntity.ModificationStatus.PENDING) {
            throw new RuntimeException("이미 수정 요청이 진행 중인 도감입니다. ID: " + wikiId);
        }
        
        log.info("원본 도감 조회 완료 - 도감명: {}", originalWiki.getName());

        WikiModificationRequest request = WikiModificationRequest.builder()
            .originalWiki(originalWiki)
            .requester(requester)
            .name(dto.getName())
            .category(dto.getCategory())
            .cultivarCode(dto.getCultivarCode())
            .flowerSize(dto.getFlowerSize())
            .petalCount(dto.getPetalCount())
            .fragrance(dto.getFragrance())
            .diseaseResistance(dto.getDiseaseResistance())
            .growthType(dto.getGrowthType())
            .usageType(dto.getUsageType())
            .recommendedPosition(dto.getRecommendedPosition())
            .continuousBlooming(dto.getContinuousBlooming())
            .multiBlooming(dto.getMultiBlooming())
            .growthPower(dto.getGrowthPower())
            .coldResistance(dto.getColdResistance())
            .imageUrl(dto.getImageUrl())
            .description(dto.getDescription()) // 수정 사유
            .build();
        
        log.info("수정 요청 객체 생성 완료 - 수정 사유: {}", dto.getDescription());

        try {
            WikiModificationRequest savedRequest = wikiModificationRequestRepository.save(request);
            
            // 원본 도감 수정 상태 변경(PENDING)
            originalWiki.setModificationStatus(WikiEntity.ModificationStatus.PENDING);
            wikiRepository.save(originalWiki);
            
            log.info("수정 요청 저장 완료 - ID: {}", savedRequest.getId());
        } catch (Exception e) {
            log.error("수정 요청 저장 실패 - 오류: {}", e.getMessage(), e);
            throw new RuntimeException("수정 요청 저장에 실패했습니다.", e);
        }

        log.info("도감 ID {} 수정 요청 제출 완료 - 관리자 승인 대기 중. 요청자: {}", wikiId, requester.getUserNick());
    }

    public List<WikiResponse> getApprovedWikiList() {
        List<WikiEntity> approvedWikis = wikiRepository.findAllByStatus(WikiEntity.Status.APPROVED);
        return approvedWikis.stream()
            .map(WikiResponse::from)
            .toList();
    }

    public WikiResponse getApprovedWikiDetail(Long id) {
        Optional<WikiEntity> wikiOptional = wikiRepository.findByIdAndStatus(id, WikiEntity.Status.APPROVED);
        
        if (wikiOptional.isPresent()) {
            WikiEntity wikiEntity = wikiOptional.get();
            return WikiResponse.from(wikiEntity);
        } else {
            log.warn("ID {}에 해당하는 승인된 도감 정보를 찾을 수 없습니다.", id);
            throw new RuntimeException("승인된 도감 정보를 찾을 수 없습니다. ID: " + id);
        }
    }

    @Transactional
    public List<WikiModificationListDto> getUserModifications(Long userId) {
        List<WikiModificationRequest> list = wikiModificationRequestRepository.findByRequesterUserNo(userId);
        return list.stream()
                .map(WikiModificationListDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public WikiModificationResubmitDto getRejectedModification(Long requestId, Long userId) {
        WikiModificationRequest request = wikiModificationRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("해당 수정 요청이 존재하지 않습니다."));

        if (!request.getRequester().getUserNo().equals(userId)) {
            throw new AccessDeniedException("본인의 요청만 조회할 수 있습니다.");
        }
        if (request.getStatus() != Status.REJECTED) {
            throw new IllegalStateException("거절된 요청만 보완할 수 있습니다.");
        }
        return WikiModificationResubmitDto.from(request);
    }

    @Transactional
    public void resubmitModificationRequest(Long requestId, Long userId, WikiModificationResubmitDto dto) {
        WikiModificationRequest request = wikiModificationRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("해당 수정 요청이 존재하지 않습니다."));

        if (!request.getRequester().getUserNo().equals(userId)) {
            throw new AccessDeniedException("본인의 요청만 수정할 수 있습니다.");
        }
        if (request.getStatus() != Status.REJECTED) {
            throw new IllegalStateException("거절된 요청만 다시 제출할 수 있습니다.");
        }

        request.setName(dto.getName());
        request.setCategory(dto.getCategory());
        request.setCultivarCode(dto.getCultivarCode());
        request.setFlowerSize(dto.getFlowerSize());
        request.setPetalCount(dto.getPetalCount());
        request.setFragrance(dto.getFragrance());
        request.setDiseaseResistance(dto.getDiseaseResistance());
        request.setGrowthType(dto.getGrowthType());
        request.setUsageType(dto.getUsageType());
        request.setRecommendedPosition(dto.getRecommendedPosition());
        request.setContinuousBlooming(dto.getContinuousBlooming());
        request.setMultiBlooming(dto.getMultiBlooming());
        request.setGrowthPower(dto.getGrowthPower());
        request.setColdResistance(dto.getColdResistance());
        request.setImageUrl(dto.getImageUrl());
        request.setDescription(dto.getDescription());

        request.setStatus(Status.PENDING);
    }

    public Page<WikiResponse> getMyWikis(
        Long userNo,
        @Nullable List<String> statusStrings,
        Pageable pageable
    ) {
        Collection<WikiEntity.Status> statuses = Collections.singletonList(WikiEntity.Status.PENDING);

        Page<WikiEntity> page = (statuses == null || statuses.isEmpty())
            ? wikiRepository.findByCreatedBy(userNo, pageable)
            : wikiRepository.findByCreatedByAndStatusIn(userNo, statuses, pageable);

        return page.map(WikiResponse::from);
    }
    
    public Page<WikiResponse> getMyRejectedWikis(Long userNo, Pageable pageable) {
        Collection<WikiEntity.Status> rejectedStatus = Collections.singletonList(WikiEntity.Status.REJECTED);
        
        Page<WikiEntity> page = wikiRepository.findByCreatedByAndStatusIn(userNo, rejectedStatus, pageable);
        return page.map(WikiResponse::from);
    }
}
