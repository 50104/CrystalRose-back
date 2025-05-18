package com.rose.back.domain.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.domain.board.entity.ImageEntity;

public interface ImageRepository extends JpaRepository<ImageEntity, Long> {

}