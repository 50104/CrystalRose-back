package com.rose.back.domain.diary.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rose.back.domain.diary.entity.CareLogEntity;

public interface CareLogRepository extends JpaRepository<CareLogEntity, Long> {

    // 날짜 범위로 케어 로그 조회 (통합 API용)
    List<CareLogEntity> findByUserNo_UserNoAndCareDateBetweenOrderByCareDateDesc(Long userNo, LocalDate startDate, LocalDate endDate);

    @Query("SELECT DISTINCT c.careDate FROM CareLogEntity c WHERE c.userNo.userNo = :userNo")
    List<LocalDate> findDistinctCareDatesByUserNo(@Param("userNo") Long userNo);

    // 사용자별 케어 로그 조회 (최신순)
    List<CareLogEntity> findByUserNo_UserNoOrderByCareDateDesc(Long userNo);

    // 특정 날짜의 케어 로그 조회
    Optional<CareLogEntity> findByUserNo_UserNoAndCareDate(Long userNo, LocalDate careDate);
}