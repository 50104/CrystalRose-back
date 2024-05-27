package com.rose.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.entity.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {

}