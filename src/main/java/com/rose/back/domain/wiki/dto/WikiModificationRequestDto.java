package com.rose.back.domain.wiki.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiModificationRequestDto {
    private Long id;
    private Long originalWikiId;
    private String requesterNick;
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
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime processedDate;
}
