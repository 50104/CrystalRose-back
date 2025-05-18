package com.rose.back.domain.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.domain.board.entity.ContentEntity;

import java.util.*;

public interface ContentRepository extends JpaRepository<ContentEntity, Long> {

    Optional<ContentEntity> findByBoardNo(Long boardNo);
    void deleteByBoardNo(Long boardNo);
    boolean existsByBoardNo(Long boardNo);
}
