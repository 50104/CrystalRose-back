package com.rose.back.domain.rose.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.rose.entity.RoseEntity;
import com.rose.back.domain.rose.entity.RoseImageEntity;

@Repository
public interface RoseImageRepository extends JpaRepository<RoseImageEntity, Long> {
    
    boolean existsByFileUrl(String fileUrl);

    List<RoseImageEntity> findByRose(RoseEntity rose);

    Optional<RoseImageEntity> findByFileUrl(String fileUrl);
}