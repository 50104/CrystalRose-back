package com.rose.back.domain.report.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.report.entity.UserBlock;
import com.rose.back.domain.user.entity.UserEntity;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    boolean existsByBlockerAndBlocked(UserEntity blocker, UserEntity blocked);
    
    @Query("SELECT ub FROM UserBlock ub JOIN FETCH ub.blocked WHERE ub.blocker = :blocker")
    List<UserBlock> findAllByBlocker(@Param("blocker") UserEntity blocker);

    @Modifying
    void deleteByBlockerAndBlocked(UserEntity blocker, UserEntity blocked);

    @Query("SELECT ub.blocked FROM UserBlock ub WHERE ub.blocker.userId = :userId")
    List<UserEntity> findAllBlockedByUserId(@Param("userId") String userId);
}
