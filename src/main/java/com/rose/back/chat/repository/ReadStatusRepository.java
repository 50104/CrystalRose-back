package com.rose.back.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.chat.entity.ReadStatus;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {
  
}
