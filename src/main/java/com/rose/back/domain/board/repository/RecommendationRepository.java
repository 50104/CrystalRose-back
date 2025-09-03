package com.rose.back.domain.board.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rose.back.domain.board.entity.RecommendationEntity;

public interface RecommendationRepository extends JpaRepository<RecommendationEntity, Long> {
  
    Optional<RecommendationEntity> findByBoardNoAndUserId(Long boardNo, String userId);

    long countByBoardNo(Long boardNo);

    @Modifying
    @Query("DELETE FROM RecommendationEntity r WHERE r.boardNo = :boardNo AND r.userId = :userId AND r.createdAt <= :oneMinuteAgo")
    int deleteByBoardNoAndUserIdAndCreatedAtBefore(@Param("boardNo") Long boardNo, @Param("userId") String userId, @Param("oneMinuteAgo") java.time.LocalDateTime oneMinuteAgo); // 1분 이전 추천 삭제
}
