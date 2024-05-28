package com.rose.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.entity.UserEmailCertification;

@Repository
public interface CertificationRepository extends JpaRepository<UserEmailCertification, String> {
    
}
