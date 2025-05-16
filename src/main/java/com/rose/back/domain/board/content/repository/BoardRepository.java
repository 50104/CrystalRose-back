package com.rose.back.domain.board.content.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.domain.board.content.entity.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {

}