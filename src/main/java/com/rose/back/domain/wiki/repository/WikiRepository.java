package com.rose.back.domain.wiki.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.wiki.entity.WikiEntity;

@Repository
public interface WikiRepository extends JpaRepository<WikiEntity, Long> {

    List<WikiEntity> findAllByStatus(WikiEntity.Status status);

    Optional<WikiEntity> findByIdAndStatus(Long id, WikiEntity.Status status);

    boolean existsByImageUrl(String imageUrl);

    // 수정 상태 조회
    List<WikiEntity> findAllByModificationStatus(WikiEntity.ModificationStatus modificationStatus);

    Optional<WikiEntity> findByIdAndModificationStatus(Long id, WikiEntity.ModificationStatus modificationStatus);

    Page<WikiEntity> findByCreatedByAndStatusIn(
        Long createdBy,
        Collection<WikiEntity.Status> statuses,
        Pageable pageable
    );

    Page<WikiEntity> findByCreatedBy(Long createdBy, Pageable pageable);

    @Query("SELECT w FROM WikiEntity w WHERE w.status = 'REJECTED' AND w.modifiedDate < :deadline")
    List<WikiEntity> findRejectedWikisBeforeDeadline(@Param("deadline") LocalDateTime deadline);
}