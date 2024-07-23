package com.rose.back.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.user.entity.UserEntity;

import java.util.*;

public interface ImgRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUserId(String userId);
}
