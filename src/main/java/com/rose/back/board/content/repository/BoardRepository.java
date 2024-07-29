package com.rose.back.board.content.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.board.content.entity.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {

}