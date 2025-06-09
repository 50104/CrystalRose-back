package com.rose.back.infra.S3;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageTempRepository extends JpaRepository<ImageTempEntity, Long> {

    List<ImageTempEntity> findByUploadedAtBefore(Date threshold);

    Optional<ImageTempEntity> findByFileUrl(String fileUrl);
}
