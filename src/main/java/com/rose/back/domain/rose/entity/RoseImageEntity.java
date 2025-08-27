package com.rose.back.domain.rose.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.rose.back.global.entity.BaseTimeEntity;

import jakarta.persistence.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "rose_mine_image")
public class RoseImageEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 100, name = "stored_file_name")
    private String storedFileName;

    @Column(length = 100, name = "original_file_name")
    private String originalFileName;

    @Column(length = 255, name = "file_url")
    private String fileUrl;

    @Column(length = 255, name = "s3_key")
    private String s3Key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rose_no")
    private RoseEntity rose;
}