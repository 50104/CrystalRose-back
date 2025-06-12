package com.rose.back.domain.diary.entity;

import java.time.LocalDateTime;

import com.rose.back.domain.rose.entity.RoseEntity;
import com.rose.back.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Setter;

@Setter
@Entity
@Builder
@Table(name = "rose_diary")
public class DiaryEntity extends BaseTimeEntity {
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private RoseEntity roseEntity;

    private String imageUrl;
    private String note;
    private LocalDateTime recordedAt; // 기록날짜

    @Column(name = "stored_file_name")
    private String storedFileName;

    @PrePersist
    public void prePersist() {
        this.recordedAt = LocalDateTime.now();
    }
}