package com.rose.back.domain.wiki.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.rose.back.domain.wiki.dto.WikiRequest;
import com.rose.back.domain.wiki.dto.WikiResponse;
import com.rose.back.domain.wiki.entity.WikiEntity;
import com.rose.back.domain.wiki.repository.WikiRepository;

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

    public void registerWiki(WikiRequest dto) {
        try {
            WikiEntity wiki = WikiEntity.builder()
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
                .imageUrl(dto.getImageUrl())
                .continuousBlooming(dto.getContinuousBlooming())
                .multiBlooming(dto.getMultiBlooming())
                .growthPower(dto.getGrowthPower())
                .coldResistance(dto.getColdResistance())
                .status(WikiEntity.Status.PENDING)
                .modificationStatus(WikiEntity.ModificationStatus.NONE)
                .build();

            wikiRepository.save(wiki);
            wikiImageService.saveAndBindImage(dto.getImageUrl(), wiki);

            log.info("도감 등록 완료 - 승인 대기 중: {}", dto.getName());

        } catch (Exception e) {
            log.error("도감 등록 중 예외 발생: {}", e.getMessage(), e);
            throw new RuntimeException("도감 등록 실패", e);
        }
    }

    public void updateWiki(Long id, WikiRequest dto) {
        WikiEntity existing = wikiRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("수정할 도감 정보를 찾을 수 없습니다. ID: " + id));

        WikiEntity updated = WikiEntity.builder()
            .id(existing.getId())
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
            .imageUrl(dto.getImageUrl())
            .continuousBlooming(dto.getContinuousBlooming())
            .multiBlooming(dto.getMultiBlooming())
            .growthPower(dto.getGrowthPower())
            .coldResistance(dto.getColdResistance())
            .status(existing.getStatus())
            .modificationStatus(WikiEntity.ModificationStatus.PENDING)
            .build();

        wikiRepository.save(updated);
        wikiImageService.saveAndBindImage(dto.getImageUrl(), updated);

        log.info("도감 ID {} 수정 완료 - 수정 검증 대기 중", id);
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
}
