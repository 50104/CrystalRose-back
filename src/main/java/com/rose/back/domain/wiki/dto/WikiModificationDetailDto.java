package com.rose.back.domain.wiki.dto;

import java.time.LocalDateTime;

import com.rose.back.domain.wiki.entity.WikiModificationRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiModificationDetailDto {
    
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
    private String continuousBlooming;
    private String multiBlooming;
    private String growthPower;
    private String coldResistance;
    private String imageUrl;
    private String description; // 수정 사유
    private LocalDateTime createdDate;
    private String status;
    
    public static WikiModificationDetailDto from(WikiModificationRequest request) {
        return WikiModificationDetailDto.builder()
            .id(request.getId())
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
            .createdDate(request.getCreatedDate())
            .status(request.getStatus().name())
            .build();
    }
}