package com.rose.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.entity.CertificationEntity;

import jakarta.transaction.Transactional;


@Repository
public interface CertificationRepository extends JpaRepository<CertificationEntity, Long> {
    
    CertificationEntity findByUserId(String userId);

    @Transactional
    void deleteByUserId(String userId);
}
