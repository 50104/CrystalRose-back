package com.rose.back.domain.diary.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rose.back.domain.diary.entity.CareLogEntity;

public interface CareLogRepository extends JpaRepository<CareLogEntity, Long> {

    @Query("SELECT DISTINCT c.careDate FROM CareLogEntity c ORDER BY c.careDate")
    List<LocalDate> findDistinctCareDates();

    List<CareLogEntity> findAllByOrderByCareDateDesc();
}