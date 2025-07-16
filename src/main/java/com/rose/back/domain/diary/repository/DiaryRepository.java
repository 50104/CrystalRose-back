package com.rose.back.domain.diary.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.domain.diary.entity.DiaryEntity;

public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {

    boolean existsByImageUrl(String imageUrl);

    List<DiaryEntity> findAllByRoseEntity_UserIdOrderByRecordedAtDesc(Long userId); // 전체 타임라인 (유저 기준)

    // 날짜 범위로 다이어리 조회
    List<DiaryEntity> findAllByRoseEntity_UserIdAndRecordedAtBetweenOrderByRecordedAtDesc(Long userId, LocalDate startDate, LocalDate endDate);

    List<DiaryEntity> findAllByRoseEntity_IdOrderByRecordedAtAsc(Long userRoseId); // 장미 타임라인 (장미 기준)
}
