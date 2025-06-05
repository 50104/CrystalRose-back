package com.rose.back.domain.wiki.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.wiki.entity.WikiEntity;

@Repository
public interface WikiRepository extends JpaRepository<WikiEntity, Long> {

    List<WikiEntity> findAllByStatus(WikiEntity.Status status);

    Optional<WikiEntity> findByIdAndStatus(Long id, WikiEntity.Status status);

    boolean existsByImageUrl(String imageUrl);

    Optional<WikiEntity> findById(Long id);
}