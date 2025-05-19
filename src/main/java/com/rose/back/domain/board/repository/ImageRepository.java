package com.rose.back.domain.board.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.board.entity.ContentEntity;
import com.rose.back.domain.board.entity.ImageEntity;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {
    
    boolean existsByFileUrl(String fileUrl);

    List<ImageEntity> findByContent(ContentEntity content);

    Optional<ImageEntity> findByFileUrl(String fileUrl);
}