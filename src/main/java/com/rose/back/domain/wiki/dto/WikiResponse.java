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
            .build();
    }
}
