package com.rose.back.domain.wiki.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.wiki.entity.WikiImageEntity;

@Repository
public interface WikiImageRepository extends JpaRepository<WikiImageEntity, Long> {
    
    boolean existsByFileUrl(String fileUrl);
}