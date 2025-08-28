package com.rose.back.domain.wiki.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.wiki.entity.WikiEntity;
import com.rose.back.domain.wiki.entity.WikiImageEntity;

@Repository
public interface WikiImageRepository extends JpaRepository<WikiImageEntity, Long> {
    
    boolean existsByFileUrl(String fileUrl);

    @Query("SELECT w FROM WikiImageEntity w WHERE w.wiki = :wiki")
    List<WikiImageEntity> findByWiki(@Param("wiki") WikiEntity wiki);
}