package com.rose.back.domain.rose.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.domain.rose.entity.RoseEntity;

public interface RoseRepository extends JpaRepository<RoseEntity, Long> {

    List<RoseEntity> findAllByUserId(Long userId);
}
