package com.rose.back.domain.wiki.repository;

import com.rose.back.domain.wiki.entity.WikiImageTempEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface WikiImageTempRepository extends JpaRepository<WikiImageTempEntity, Long> {

    Optional<WikiImageTempEntity> findByFileUrl(String fileUrl);

    List<WikiImageTempEntity> findByUploadedAtBefore(Date threshold);

    boolean existsByFileUrl(String fileUrl);
}
