package com.rose.back.domain.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.auth.entity.EmailCertificationEntity;

import jakarta.persistence.LockModeType;

@Repository
public interface EmailCertificationRepository extends JpaRepository<EmailCertificationEntity, Long> {

    // 검증용
    @Lock(LockModeType.PESSIMISTIC_WRITE) // 동시 검증 시 race condition 방지
    Optional<EmailCertificationEntity> findByUserIdAndUserEmailAndUsedFalse(String userId, String userEmail);

    // 기존 미사용 레코드 마크 처리
    @Modifying
    @Query("UPDATE EmailCertificationEntity e SET e.used = true, e.usedAt = :usedAt WHERE e.userId = :userId AND e.userEmail = :userEmail AND e.used = false")
    int markAllAsUsedByUserIdAndUserEmail(@Param("userId") String userId, @Param("userEmail") String userEmail, @Param("usedAt") LocalDateTime usedAt);
}