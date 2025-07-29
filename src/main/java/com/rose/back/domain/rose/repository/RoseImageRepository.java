package com.rose.back.domain.rose.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.rose.entity.RoseImageEntity;

@Repository
public interface RoseImageRepository extends JpaRepository<RoseImageEntity, Long> {
    
    boolean existsByFileUrl(String fileUrl);

    void deleteByRoseId(Long roseId);

    List<RoseImageEntity> findByRoseId(Long roseId);
}