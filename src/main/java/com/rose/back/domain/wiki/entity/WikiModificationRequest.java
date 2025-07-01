package com.rose.back.domain.wiki.entity;

import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rose_wiki_modification_request")
public class WikiModificationRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_wiki_id", nullable = false)
    private WikiEntity originalWiki;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private UserEntity requester;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(name = "cultivar_code", nullable = false)
    private String cultivarCode;

    @Column(name = "flower_size", nullable = false)
    private String flowerSize;

    @Column(name = "petal_count", nullable = false)
    private String petalCount;

    @Column(nullable = false)
    private String fragrance;

    @Column(name = "disease_resistance", nullable = false)
    private String diseaseResistance;

    @Column(name = "growth_type", nullable = false)
    private String growthType;

    @Column(name = "usage_type", nullable = false)
    private String usageType;

    @Column(name = "recommended_position", nullable = false)
    private String recommendedPosition;

    @Column(name = "continuous_blooming", nullable = false)
    private String continuousBlooming;

    @Column(name = "multi_blooming", nullable = false)
    private String multiBlooming;

    @Column(name = "growth_power", nullable = false)
    private String growthPower;

    @Column(name = "cold_resistance", nullable = false)
    private String coldResistance;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description; // 수정 사유

}
