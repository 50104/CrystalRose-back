package com.rose.back.domain.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.report.entity.UserBlock;
import com.rose.back.domain.user.entity.UserEntity;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    boolean existsByBlockerAndBlocked(UserEntity blocker, UserEntity blocked);
}
