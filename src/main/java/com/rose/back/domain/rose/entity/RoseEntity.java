package com.rose.back.domain.rose.entity;

import java.time.LocalDate;

import com.rose.back.domain.wiki.entity.WikiEntity;
import com.rose.back.global.entity.BaseTimeEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    private WikiEntity wikiEntity;

    private Long userId;  // 사용자 ID
    private String nickname; // 사용자 지정 별명
    private LocalDate acquiredDate; // 획득 날짜
    private String locationNote; // 메모
    private String imageUrl;
}