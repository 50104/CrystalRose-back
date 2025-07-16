package com.rose.back.domain.diary.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rose.back.domain.rose.entity.RoseEntity;
import com.rose.back.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rose_diary")
public class DiaryEntity extends BaseTimeEntity {
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rose_id", nullable = false)
    private RoseEntity roseEntity;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;
    private String note;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "recorded_at", nullable = false)
    private LocalDate recordedAt; // 기록날짜

    @Column(name = "stored_file_name")
    private String storedFileName;

    @PrePersist
    public void prePersist() {
        if (this.recordedAt == null) {
            this.recordedAt = LocalDate.now();
        }
    }
}