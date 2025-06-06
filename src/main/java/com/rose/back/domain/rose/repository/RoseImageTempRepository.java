package com.rose.back.domain.rose.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.domain.rose.entity.RoseImageTempEntity;

public interface RoseImageTempRepository extends JpaRepository<RoseImageTempEntity, Long> {

    Optional<RoseImageTempEntity> findByFileUrl(String fileUrl);

    List<RoseImageTempEntity> findByUploadedAtBefore(Date threshold);
}
