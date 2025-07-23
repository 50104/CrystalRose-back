package com.rose.back.domain.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.diary.entity.DiaryImageEntity;

@Repository
public interface DiaryImageRepository extends JpaRepository<DiaryImageEntity, Long> {
    
    @Modifying
    @Query("DELETE FROM DiaryImageEntity d WHERE d.diary.id = :diaryId")
    int deleteByDiaryId(@Param("diaryId") Long diaryId);
}