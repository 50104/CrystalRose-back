package com.rose.back.domain.rose.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rose.back.domain.rose.entity.RoseEntity;

public interface RoseRepository extends JpaRepository<RoseEntity, Long> {

    List<RoseEntity> findAllByUserId(Long userId);

    boolean existsByImageUrl(String imageUrl);
    
    @Query("SELECT r FROM RoseEntity r JOIN FETCH r.wikiEntity WHERE r.userId = :userId ORDER BY r.acquiredDate DESC")
    List<RoseEntity> findByUserIdOrderByAcquiredDateDesc(@Param("userId") Long userId);
    
    @Query("SELECT r FROM RoseEntity r JOIN FETCH r.wikiEntity WHERE r.id = :id AND r.userId = :userId")
    Optional<RoseEntity> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}