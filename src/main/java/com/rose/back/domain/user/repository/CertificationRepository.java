package com.rose.back.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.user.entity.CertificationEntity;


@Repository
public interface CertificationRepository extends JpaRepository<CertificationEntity, Long> {

    Optional<CertificationEntity> findByUserIdAndUserEmail(String userId, String userEmail);

    void deleteByUserIdAndUserEmail(String userId, String userEmail);
}
