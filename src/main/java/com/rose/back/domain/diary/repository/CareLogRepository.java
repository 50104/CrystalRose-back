package com.rose.back.domain.diary.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rose.back.domain.diary.entity.CareLogEntity;

public interface CareLogRepository extends JpaRepository<CareLogEntity, Long> {

    List<CareLogEntity> findByUserNo_UserNoOrderByCareDateDesc(Long userNo);

    @Query("SELECT DISTINCT c.careDate FROM CareLogEntity c WHERE c.userNo.userNo = :userNo")
    List<LocalDate> findDistinctCareDatesByUserNo(@Param("userNo") Long userNo);
}