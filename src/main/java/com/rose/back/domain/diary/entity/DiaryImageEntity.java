package com.rose.back.domain.diary.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.rose.back.global.entity.BaseTimeEntity;

import jakarta.persistence.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "rose_diary_image")
public class DiaryImageEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 100, name = "stored_file_name")
    private String storedFileName;

    @Column(length = 100, name = "original_file_name")
    private String originalFileName;

    @Column(length = 255, name = "file_url")
    private String fileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_no")
    private DiaryEntity diary;
}