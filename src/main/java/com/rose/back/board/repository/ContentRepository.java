package com.rose.back.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.board.entity.ContentEntity;

public interface ContentRepository extends JpaRepository<ContentEntity, Integer> {

}
