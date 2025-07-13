package com.rose.back.domain.rose.entity;

import java.time.LocalDate;

import com.rose.back.domain.wiki.entity.WikiEntity;
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

@Builder
@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rose_mine")
public class RoseEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wiki_id", nullable = false)
    private WikiEntity wikiEntity;

    @Column(name = "user_id", nullable = false)
    private Long userId;  // 사용자 ID

    @Column(length = 100, name = "rose_nickname", nullable = false)
    private String nickname; // 사용자 지정 별명

    @Column(name = "acquired_date", nullable = false)
    private LocalDate acquiredDate; // 획득 날짜

    @Column(length = 255, name = "location_note")
    private String locationNote; // 메모

    @Column(length = 255, name = "image_url", nullable = false)
    private String imageUrl;
}