package com.rose.back.domain.board.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rose.back.domain.board.entity.ContentEntity;

import java.util.*;

public interface ContentRepository extends JpaRepository<ContentEntity, Long> {

    Optional<ContentEntity> findByBoardNo(Long boardNo);

    void deleteByBoardNo(Long boardNo);
    
    boolean existsByBoardNo(Long boardNo);

    long countByIsFixedTrue();

    @Query("SELECT c FROM ContentEntity c WHERE c.isFixed = true ORDER BY c.boardNo DESC")
    List<ContentEntity> findByIsFixedTrueOrderByBoardNoDesc();

    @Query("SELECT c FROM ContentEntity c WHERE c.isFixed = false")
    Page<ContentEntity> findByIsFixedFalse(Pageable pageable);

    @Query("SELECT c FROM ContentEntity c WHERE c.boardNo < :boardNo ORDER BY c.boardNo DESC")
    List<ContentEntity> findPrevPost(@Param("boardNo") Long boardNo, Pageable pageable);

    @Query("SELECT c FROM ContentEntity c WHERE c.boardNo > :boardNo ORDER BY c.boardNo ASC")
    List<ContentEntity> findNextPost(@Param("boardNo") Long boardNo, Pageable pageable);

    @Query(value = "select c from ContentEntity c left join fetch c.writer", countQuery = "select count(c) from ContentEntity c")
    Page<ContentEntity> findPageWithWriter(Pageable pageable);

    @Query("UPDATE ContentEntity c SET c.recommendCount = c.recommendCount + 1 WHERE c.boardNo = :boardNo")
    @Modifying
    int incrementRecommendCount(@Param("boardNo") Long boardNo); // 추천 수 증가

    @Query("UPDATE ContentEntity c SET c.recommendCount = c.recommendCount - 1 WHERE c.boardNo = :boardNo AND c.recommendCount > 0")
    @Modifying
    int decrementRecommendCount(@Param("boardNo") Long boardNo); // 추천 수 감소
}