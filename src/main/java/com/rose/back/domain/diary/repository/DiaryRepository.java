package com.rose.back.domain.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.domain.diary.entity.DiaryEntity;

public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {

    boolean existsByImageUrl(String imageUrl);
}
