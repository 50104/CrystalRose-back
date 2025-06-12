package com.rose.back.domain.diary.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.domain.diary.entity.DiaryEntity;

public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {

    boolean existsByImageUrl(String imageUrl);

    List<DiaryEntity> findAllByRoseEntity_UserIdOrderByRecordedAtDesc(Long userId); // 전체 타임라인 (유저 기준)
}
