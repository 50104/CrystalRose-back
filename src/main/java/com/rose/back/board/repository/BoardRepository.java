package com.rose.back.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.board.entity.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {

}