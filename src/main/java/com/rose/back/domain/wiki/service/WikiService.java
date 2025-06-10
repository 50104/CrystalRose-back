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

    private final WikiRepository roseWikiRepository;
    private final WikiImageService wikiImageService;

    public void registerWiki(WikiRequest dto) {
        try {
            WikiEntity wiki = WikiEntity.builder()
                .name(dto.getName())
                .category(dto.getCategory())
                .cultivarCode(dto.getCultivarCode())
                .description(dto.getDescription())
                .flowerSize(dto.getFlowerSize())
                .petalCount(dto.getPetalCount())
                .fragrance(dto.getFragrance())
                .diseaseResistance(dto.getDiseaseResistance())
                .growthType(dto.getGrowthType())
                .usageType(dto.getUsageType())
                .recommendedPosition(dto.getRecommendedPosition())
                .imageUrl(dto.getImageUrl())
                .status(WikiEntity.Status.PENDING)
                .build();
            roseWikiRepository.save(wiki);
            wikiImageService.saveAndBindImage(dto.getImageUrl(), wiki);
        } catch (Exception e) {
            log.error("도감 등록 중 예외 발생: {}", e.getMessage(), e);
            throw new RuntimeException("도감 등록 실패", e);
        }
    }
  
    public List<WikiResponse> getApprovedWikiList() {
        List<WikiEntity> approvedWikis = roseWikiRepository.findAllByStatus(WikiEntity.Status.APPROVED);
        return approvedWikis.stream()
            .map(WikiResponse::from)
            .toList();
    }

    public WikiResponse getApprovedWikiDetail(Long id) {
        Optional<WikiEntity> wikiOptional = roseWikiRepository.findByIdAndStatus(id, WikiEntity.Status.APPROVED);
        
        if (wikiOptional.isPresent()) {
            WikiEntity wikiEntity = wikiOptional.get();
            return WikiResponse.from(wikiEntity);
        } else {
            log.warn("ID {}에 해당하는 승인된 도감 정보를 찾을 수 없습니다.", id);
            throw new RuntimeException("승인된 도감 정보를 찾을 수 없습니다. ID: " + id);
        }
    }
}
