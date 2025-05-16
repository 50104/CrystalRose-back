package com.rose.back.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.domain.user.entity.UserEntity;

import java.util.*;

public interface ImgRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUserId(String userId);
}
