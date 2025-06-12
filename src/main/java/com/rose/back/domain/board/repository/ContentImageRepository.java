package com.rose.back.domain.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.board.entity.ContentImageEntity;
import com.rose.back.domain.board.entity.ContentEntity;

@Repository
public interface ContentImageRepository extends JpaRepository<ContentImageEntity, Long> {
    
    boolean existsByFileUrl(String fileUrl);

    List<ContentImageEntity> findByContent(ContentEntity content);
}