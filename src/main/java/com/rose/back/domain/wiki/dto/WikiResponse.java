package com.rose.back.domain.wiki.dto;

import com.rose.back.domain.wiki.entity.WikiEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WikiResponse {
    private Long id;
    private String name;
    private String category;
    private String cultivarCode;
    private String flowerSize;
    private String petalCount;
    private String fragrance;
    private String diseaseResistance;
    private String growthType;
    private String usageType;
    private String recommendedPosition;
    private String imageUrl;
    private String continuousBlooming;
    private String multiBlooming;
    private String growthPower;
    private String coldResistance;
    private String status; // 도감 전체 상태
    private String modificationStatus; // 수정 상태
    private String rejectionReason; // 거절 사유
    private Long createdBy; // 작성자

    public static WikiResponse from(WikiEntity wiki) {
        return WikiResponse.builder()
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
            .imageUrl(wiki.getImageUrl())
            .continuousBlooming(wiki.getContinuousBlooming())
            .multiBlooming(wiki.getMultiBlooming())
            .growthPower(wiki.getGrowthPower())
            .coldResistance(wiki.getColdResistance())
            .status(wiki.getStatus() != null ? wiki.getStatus().name() : null)
            .modificationStatus(wiki.getModificationStatus() != null ? wiki.getModificationStatus().name() : null)
            .rejectionReason(wiki.getRejectionReason())
            .createdBy(wiki.getCreatedBy())
            .build();
    }
}
