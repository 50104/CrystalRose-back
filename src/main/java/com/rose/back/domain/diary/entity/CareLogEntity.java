package com.rose.back.domain.diary.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.global.entity.BaseTimeEntity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rose_care_log")
public class CareLogEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no", nullable = false)
    private UserEntity userNo;

    private LocalDate careDate;

    private String fertilizer;  // 영양제
    private String pesticide;   // 살충제
    private String adjuvant;    // 보조제
    private String compost;     // 비료
    private String fungicide;   // 살균제

    private String note;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}