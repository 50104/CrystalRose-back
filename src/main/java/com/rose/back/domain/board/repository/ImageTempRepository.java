package com.rose.back.domain.board.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.board.entity.ImageTempEntity;

@Repository
public interface ImageTempRepository extends JpaRepository<ImageTempEntity, Long> {

    List<ImageTempEntity> findByUploadedAtBefore(Date time);

    void deleteByFileUrl(String fileUrl);

    Optional<ImageTempEntity> findByFileUrl(String fileUrl);
}
