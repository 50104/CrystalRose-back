package com.rose.back.domain.wiki.entity;

import com.rose.back.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rose_wiki")
public class WikiEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 품종명

    private String category; // 품종 카테고리

    @Column(name = "cultivar_code")
    private String cultivarCode; // 품종코드

    @Column(name = "flower_size")
    private String flowerSize; // 꽃 크기

    @Column(name = "petal_count")
    private String petalCount; // 꽃잎 수

    @Column(name = "fragrance")
    private String fragrance; // 향기

    @Column(name = "disease_resistance")
    private String diseaseResistance; // 내병성

    @Column(name = "cold_resistance")
    private String coldResistance; // 내한성

    @Column(name = "growth_type")
    private String growthType; // 생장형태

    @Column(name = "usage_type")
    private String usageType; // 사용 용도

    @Column(name = "recommended_position")
    private String recommendedPosition; // 추천 위치

    @Column(name = "image_url")
    private String imageUrl; // 대표 이미지

    @Column(name = "continuous_blooming")
    private String continuousBlooming; // 연속개화성

    @Column(name = "multi_blooming")
    private String multiBlooming; // 다화성

    @Column(name = "growth_power")
    private String growthPower; // 수세

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}
