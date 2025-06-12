package com.rose.back.domain.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rose.back.domain.diary.entity.DiaryImageEntity;

@Repository
public interface DiaryImageRepository extends JpaRepository<DiaryImageEntity, Long> {
}