package com.rose.back.domain.auth.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.user.entity.UserEntity;

@Repository
public interface AuthRepository extends JpaRepository<UserEntity, Long> {
  
    UserEntity findByUserId(String userId);
  
    List<UserEntity> findByReservedDeleteAtBefore(LocalDateTime time);
}
