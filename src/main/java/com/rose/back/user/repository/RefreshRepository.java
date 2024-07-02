package com.rose.back.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.user.entity.RefreshEntity;

import jakarta.transaction.Transactional;

public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {

    Boolean existsByRefresh(String refresh);

    @Transactional
    void deleteByRefresh(String refresh);
}