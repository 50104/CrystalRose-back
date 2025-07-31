package com.rose.back.domain.board.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.domain.board.entity.RecommendationEntity;

public interface RecommendationRepository extends JpaRepository<RecommendationEntity, Long> {
  
    Optional<RecommendationEntity> findByBoardNoAndUserId(Long boardNo, String userId);

    long countByBoardNo(Long boardNo);
}
