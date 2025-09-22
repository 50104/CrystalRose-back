package com.rose.back.domain.wiki.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.wiki.entity.WikiModificationRequest;

@Repository
public interface WikiModificationRequestRepository extends JpaRepository<WikiModificationRequest, Long> {

  List<WikiModificationRequest> findAllByRequesterUserNoAndStatus(Long userNo, WikiModificationRequest.Status status);

  List<WikiModificationRequest> findByRequesterUserNo(Long userNo);

  @Query("SELECT w FROM WikiModificationRequest w WHERE w.status = :status AND w.modifiedDate < :thresholdDate")
  List<WikiModificationRequest> findByStatusAndModifiedDateBefore(
      @Param("status") WikiModificationRequest.Status status,
      @Param("thresholdDate") LocalDateTime thresholdDate
  );
}
