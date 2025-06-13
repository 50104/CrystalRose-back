package com.rose.back.domain.wiki.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WikiRequest {

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
}
