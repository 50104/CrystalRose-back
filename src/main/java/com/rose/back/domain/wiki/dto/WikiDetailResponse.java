package com.rose.back.domain.wiki.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WikiDetailResponse {
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
    private String status;
    private String modificationStatus;
    private LocalDateTime createdDate;
}
