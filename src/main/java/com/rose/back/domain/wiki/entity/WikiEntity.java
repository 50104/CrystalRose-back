package com.rose.back.domain.wiki.entity;

import com.rose.back.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private String description; // 품종 설명

    @Column(name = "flower_size")
    private String flowerSize; // 꽃 크기

    @Column(name = "petal_count")
    private String petalCount; // 꽃잎 수

    @Column(name = "fragrance")
    private String fragrance; // 향기

    @Column(name = "disease_resistance")
    private String diseaseResistance; // 병해 저항성(내병성)

    @Column(name = "growth_type")
    private String growthType; // 생장형태

    @Column(name = "usage_type")
    private String usageType; // 사용 용도

    @Column(name = "recommended_position")
    private String recommendedPosition; // 추천 위치

    @Column(name = "image_url")
    private String imageUrl; // 대표 이미지

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}