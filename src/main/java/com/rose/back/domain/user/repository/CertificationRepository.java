package com.rose.back.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.user.entity.CertificationEntity;

import jakarta.transaction.Transactional;


@Repository
public interface CertificationRepository extends JpaRepository<CertificationEntity, Long> {
    
    CertificationEntity findByUserId(String userId);

    @Transactional
    void deleteByUserEmail(String userEmail);
}
