package com.rose.back.domain.wiki.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.wiki.entity.WikiModificationRequest;

@Repository
public interface WikiModificationRequestRepository extends JpaRepository<WikiModificationRequest, Long> {

  List<WikiModificationRequest> findAllByRequesterUserNoAndStatus(Long userNo, WikiModificationRequest.Status status);
}
